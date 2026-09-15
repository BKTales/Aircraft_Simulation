package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.dsl.api.SimulatorAircraftJsonMapper;
import eapli.aisafe.dsl.api.SimulatorAirportJsonMapper;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightmanagement.application.SimulatorFlightPlanJsonBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enriches LAPR4 client JSON (segments + string {@code Aircraft}) for the C simulator export.
 * Converts LAPR4 {@code Flight Profile} measures to C {@code FlightProfile} format.
 * Stored {@code jsonContent} stays unchanged; enrichment runs only when writing temp plan files.
 */
public final class Lapr4FlightPlanSimulatorEnricher {

    private static final Pattern STRING_AIRCRAFT =
            Pattern.compile("\"Aircraft\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern LEG_PAYLOAD_KG =
            Pattern.compile("\"Payload\"\\s*:\\s*\\{\\s*\"Quantity\"\\s*:\\s*([0-9.]+)");
    private static final Pattern LAPR4_ALTITUDE =
            Pattern.compile("\"Altitude\"\\s*:\\s*\\{\\s*\"Value\"\\s*:\\s*([0-9.]+)[^}]*}");
    private static final Pattern LAPR4_SPEED_KT =
            Pattern.compile("\"Speed\"\\s*:\\s*\\{\\s*\"Value\"\\s*:\\s*([0-9.]+)[^}]*}");

    private final AircraftModelRepository aircraftModels;
    private final EngineModelRepository engineModels;
    private final SimulatorFlightPlanJsonBuilder jsonBuilder;

    public Lapr4FlightPlanSimulatorEnricher(final AircraftModelRepository aircraftModels,
                                            final EngineModelRepository engineModels,
                                            final SimulatorFlightPlanJsonBuilder jsonBuilder) {
        this.aircraftModels = aircraftModels;
        this.engineModels = engineModels;
        this.jsonBuilder = jsonBuilder;
    }

    public String enrich(final String lapr4Json,
                         final Aircraft aircraft,
                         final FlightPlanDescriptor descriptor) {
        if (lapr4Json == null || lapr4Json.isBlank()) {
            throw new IllegalArgumentException("LAPR4 JSON is required.");
        }
        if (Lapr4FlightPlanJson.isSimulatorReady(lapr4Json)) {
            return lapr4Json;
        }
        if (!Lapr4FlightPlanJson.hasSegmentPlan(lapr4Json)) {
            return lapr4Json;
        }

        String enriched = lapr4Json;
        enriched = replaceStringAircraft(enriched, aircraft);
        enriched = insertLoadIfMissing(enriched, extractPayloadKg(enriched));
        enriched = convertLapr4FlightProfile(enriched);
        enriched = insertLegAirports(enriched, descriptor);
        return enriched;
    }

    private String replaceStringAircraft(final String json, final Aircraft aircraft) {
        final Matcher matcher = STRING_AIRCRAFT.matcher(json);
        if (!matcher.find()) {
            return json;
        }
        final AircraftModel model = aircraftModels.ofIdentity(aircraft.aircraftModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown aircraft model: " + aircraft.aircraftModelId()));
        final EngineModel engine = engineModels.findByModelId(aircraft.engineModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown engine model: " + aircraft.engineModelId()));
        final String aircraftObject = SimulatorAircraftJsonMapper.toJsonObject(model, engine);
        final String replacement = "\"AircraftId\": \"" + matcher.group(1) + "\",\n    \"Aircraft\": " + aircraftObject;
        return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }

    private static double extractPayloadKg(final String json) {
        final Matcher matcher = LEG_PAYLOAD_KG.matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    }

    private static String insertLoadIfMissing(final String json, final double payloadKg) {
        if (json.contains("\"Load\"") || payloadKg <= 0) {
            return json;
        }
        final int legIndex = json.indexOf("\"Leg\"");
        if (legIndex < 0) {
            return json;
        }
        final String load = """
                "Load": {
                    "PassengerCount": 0,
                    "PassengerWeight": { "Quantity": 0, "Unit": "kg" },
                    "CargoWeight": { "Quantity": %s, "Unit": "kg" },
                    "TotalPayloadMassKg": %s
                },
                """.formatted(trim(payloadKg), trim(payloadKg));
        String prefix = json.substring(0, legIndex).stripTrailing();
        if (!prefix.endsWith(",")) {
            prefix = prefix + ",";
        }
        return prefix + "\n    " + load + "\n    " + json.substring(legIndex);
    }

    /**
     * C simulator expects {@code FlightProfile} with numeric {@code Altitude}/{@code IAS};
     * LAPR4 client JSON nests measures under {@code Value}/{@code Unit}.
     * Leg fuel quantity is passed through as-is (C uses {@code quantity} as kg).
     */
    private static String convertLapr4FlightProfile(final String json) {
        if (!json.contains("\"Flight Profile\"") && !json.contains("\"Altitude\": {")) {
            return json;
        }
        String converted = json.replace("\"Flight Profile\"", "\"FlightProfile\"");
        converted = LAPR4_ALTITUDE.matcher(converted).replaceAll("\"Altitude\": $1");
        converted = LAPR4_SPEED_KT.matcher(converted).replaceAll("\"IAS\": $1");
        return converted;
    }

    private String insertLegAirports(final String json, final FlightPlanDescriptor descriptor) {
        if (json.contains("\"DepartureAirport\"") || descriptor.getLegs().isEmpty()) {
            return json;
        }
        final LegDescriptor leg = descriptor.getLegs().get(0);
        final Airport departure = jsonBuilder.requireAirportForCode(leg.getDepartureAirport());
        final Airport arrival = jsonBuilder.requireAirportForCode(leg.getArrivalAirport());
        final String airports = ",\n        \"DepartureAirport\": "
                + SimulatorAirportJsonMapper.toJsonObject(departure)
                + ",\n        \"ArrivalAirport\": "
                + SimulatorAirportJsonMapper.toJsonObject(arrival);
        final int segmentsClose = findLegSegmentsClose(json);
        if (segmentsClose < 0) {
            return json;
        }
        return json.substring(0, segmentsClose) + airports + json.substring(segmentsClose);
    }

  /** Index after the closing {@code ]} of the first leg's {@code Segments} array. */
    private static int findLegSegmentsClose(final String json) {
        final int segmentsKey = json.indexOf("\"Segments\"");
        if (segmentsKey < 0) {
            return -1;
        }
        int depth = 0;
        boolean started = false;
        for (int i = segmentsKey; i < json.length(); i++) {
            final char c = json.charAt(i);
            if (c == '[') {
                depth++;
                started = true;
            } else if (c == ']') {
                depth--;
                if (started && depth == 0) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private static String trim(final double value) {
        final long asLong = (long) value;
        return value == asLong ? Long.toString(asLong) : Double.toString(value);
    }
}
