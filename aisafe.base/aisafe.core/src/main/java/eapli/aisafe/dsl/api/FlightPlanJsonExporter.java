package eapli.aisafe.dsl.api;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;

import java.util.List;
import java.util.Locale;

public final class FlightPlanJsonExporter {

    private static final double LITRES_TO_KG = 0.804;

    private static final String DEFAULT_FLIGHT_PROFILE = """
"FlightProfile": {
  "Climb": [
    { "Altitude": 0,     "IAS": 200 },
    { "Altitude": 1000,  "IAS": 210 },
    { "Altitude": 2000,  "IAS": 220 },
    { "Altitude": 3000,  "IAS": 230 },
    { "Altitude": 4000,  "IAS": 245 },
    { "Altitude": 5000,  "IAS": 255 },
    { "Altitude": 6000,  "IAS": 265 },
    { "Altitude": 7000,  "IAS": 275 },
    { "Altitude": 8000,  "IAS": 285 },
    { "Altitude": 9000,  "IAS": 295 },
    { "Altitude": 10000, "IAS": 300 },
    { "Altitude": 11000, "IAS": 300 }
  ],
  "Descend": [
    { "Altitude": 0,     "IAS": 145 },
    { "Altitude": 1000,  "IAS": 170 },
    { "Altitude": 2000,  "IAS": 200 },
    { "Altitude": 3000,  "IAS": 230 },
    { "Altitude": 4000,  "IAS": 260 },
    { "Altitude": 5000,  "IAS": 280 },
    { "Altitude": 6000,  "IAS": 285 },
    { "Altitude": 7000,  "IAS": 290 },
    { "Altitude": 8000,  "IAS": 295 },
    { "Altitude": 9000,  "IAS": 300 },
    { "Altitude": 10000, "IAS": 300 },
    { "Altitude": 11000, "IAS": 300 }
  ]
}""";

    private FlightPlanJsonExporter() {}

    public static String toSimulatorJson(final FlightPlanDescriptor d) {
        return toSimulatorJson(d, d.getFlightId());
    }

    public static String toSimulatorJson(final FlightPlanDescriptor d, final String aircraftId) {
        throw new IllegalArgumentException(
                "Self-contained simulator JSON requires aircraft model, engine and airports.");
    }

    public static String toSimulatorJson(final FlightPlanDescriptor d,
                                         final SimulatorFlightPlanJsonContext context) {
        if (d == null || context == null) {
            throw new IllegalArgumentException("Descriptor and context are required.");
        }

        final StringBuilder sb = new StringBuilder(8192);
        sb.append("{\n");
        sb.append("  \"ID\": ").append(stableId(d.getFlightId())).append(",\n");
        sb.append("  \"Type\": ").append(q(d.getFlightType().toLowerCase(Locale.ROOT))).append(",\n");
        sb.append("  \"Route\": ").append(q(routeLabel(d))).append(",\n");

        appendLoad(sb, d);
        sb.append(",\n");

        sb.append("  \"AircraftId\": ").append(q(context.aircraftId())).append(",\n");
        sb.append("  \"Aircraft\": ").append(SimulatorAircraftJsonMapper.toJsonObject(
                context.aircraftModel(), context.engineModel())).append(",\n");
        sb.append("  \"Legs\": [");
        final List<LegDescriptor> legs = d.getLegs();
        for (int i = 0; i < legs.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\n    ");
            appendLeg(sb, legs.get(i), d, context.airportsByIata());
        }
        if (!legs.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("]\n}");
        return sb.toString();
    }

    private static void appendLeg(final StringBuilder sb,
                                    final LegDescriptor leg,
                                    final FlightPlanDescriptor descriptor,
                                    final java.util.Map<String, Airport> airportsByIata) {
        final Airport departure = requireAirport(airportsByIata, leg.getDepartureAirport());
        final Airport arrival = requireAirport(airportsByIata, leg.getArrivalAirport());

        sb.append("{\n");
        sb.append("      \"Departure\": ").append(q(leg.getDepartureAirport())).append(",\n");
        sb.append("      \"DepartureAirport\": ").append(SimulatorAirportJsonMapper.toJsonObject(departure)).append(",\n");
        sb.append("      \"Arrival\": ").append(q(leg.getArrivalAirport())).append(",\n");
        sb.append("      \"ArrivalAirport\": ").append(SimulatorAirportJsonMapper.toJsonObject(arrival)).append(",\n");
        sb.append("      \"DepartureTime\": ").append(q(leg.getDepartureTime())).append(",\n");
        sb.append("      \"ArrivalTime\": ").append(q(leg.getArrivalTime())).append(",\n");

        appendFuel(sb, leg);

        final String indentedProfile = "      " + DEFAULT_FLIGHT_PROFILE.replace("\n", "\n      ");
        sb.append(indentedProfile).append(",\n");

        final List<SegmentDescriptor> segments = leg.getRoute().segments();
        sb.append("      \"Segments\": [");
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\n        ");
            appendSegment(sb, segments.get(i), segmentMode(i, segments.size()));
        }
        if (!segments.isEmpty()) {
            sb.append("\n      ");
        }
        sb.append("]\n    }");
    }

    private static Airport requireAirport(final java.util.Map<String, Airport> airportsByCode, final String code) {
        final Airport airport = airportsByCode.get(code);
        if (airport == null) {
            throw new IllegalArgumentException("Unknown airport code: " + code);
        }
        return airport;
    }

    private static void appendFuel(final StringBuilder sb, final LegDescriptor leg) {
        double qty = leg.getFuelValue();
        String unit = leg.getFuelUnit().toLowerCase(Locale.ROOT);
        if ("l".equals(unit)) {
            qty = qty * LITRES_TO_KG;
            unit = "kg";
        }
        sb.append("      \"Fuel\": { \"Quantity\": ").append(trimDouble(qty))
                .append(", \"Unit\": ").append(q(unit)).append(" },\n");
    }

    private static void appendLoad(final StringBuilder sb, final FlightPlanDescriptor descriptor) {
        sb.append("  \"Load\": {\n");
        sb.append("    \"PassengerCount\": ").append(descriptor.getPassengersCount()).append(",\n");
        sb.append("    \"PassengerWeight\": { \"Quantity\": ").append(trimDouble(descriptor.getPassengerWeightKg()))
                .append(", \"Unit\": \"kg\" },\n");
        sb.append("    \"CargoWeight\": { \"Quantity\": ").append(trimDouble(descriptor.getCargoWeightKg()))
                .append(", \"Unit\": \"kg\" },\n");
        sb.append("    \"TotalPayloadMassKg\": ").append(trimDouble(descriptor.getLoadWeight())).append("\n");
        sb.append("  }");
    }

    private static void appendSegment(final StringBuilder sb, final SegmentDescriptor s, final String mode) {
        final int startAlt = pickStartAltitudeMetres(s);
        final int endAlt = pickEndAltitudeMetres(s, mode);
        sb.append("{\n");
        sb.append("          \"Mode\": ").append(q(mode)).append(",\n");
        sb.append("          \"Start\": {\n");
        sb.append("            \"Latitude\": ").append(trimDouble(s.startLatitude())).append(",\n");
        sb.append("            \"Longitude\": ").append(trimDouble(s.startLongitude())).append(",\n");
        sb.append("            \"Altitude\": { \"Quantity\": ").append(startAlt).append(", \"Unit\": \"m\" }\n");
        sb.append("          },\n");
        sb.append("          \"End\": {\n");
        sb.append("            \"Latitude\": ").append(trimDouble(s.endLatitude())).append(",\n");
        sb.append("            \"Longitude\": ").append(trimDouble(s.endLongitude())).append(",\n");
        sb.append("            \"Altitude\": { \"Quantity\": ").append(endAlt).append(", \"Unit\": \"m\" }\n");
        sb.append("          },\n");
        sb.append("          \"WindDirectionDeg\": ").append(s.windDirectionDegrees()).append(",\n");
        sb.append("          \"WindSpeedMs\": ").append(trimDouble(s.windSpeedMetresPerSecond())).append("\n");
        sb.append("        }");
    }

    private static String segmentMode(final int index, final int total) {
        if (total <= 1) return "cruise";
        if (index == 0) return "climb";
        if (index == total - 1) return "descend";
        return "cruise";
    }

    private static String routeLabel(final FlightPlanDescriptor d) {
        if (d.getLegs().isEmpty()) return d.getFlightId();
        final LegDescriptor first = d.getLegs().get(0);
        final LegDescriptor last = d.getLegs().get(d.getLegs().size() - 1);
        return first.getDepartureAirport() + "-" + last.getArrivalAirport();
    }

    private static int pickStartAltitudeMetres(final SegmentDescriptor s) {
        final List<AltitudeSlotDescriptor> slots = s.altitudeSlots();
        if (slots.isEmpty()) {
            return 0;
        }
        return Math.max(0, slots.get(0).altitudeMetres());
    }

    private static int pickEndAltitudeMetres(final SegmentDescriptor s, final String mode) {
        final List<AltitudeSlotDescriptor> slots = s.altitudeSlots();
        if (slots.isEmpty()) {
            return 0;
        }
        if ("cruise".equals(mode)) {
            return Math.max(0, slots.get(0).altitudeMetres());
        }
        return Math.max(0, slots.get(slots.size() - 1).altitudeMetres());
    }

    private static int stableId(final String flightId) {
        if (flightId == null) return 0;
        long h = 1125899906842597L;
        for (int i = 0; i < flightId.length(); i++) {
            h = 31 * h + flightId.charAt(i);
        }
        return (int) (Math.abs(h) % Integer.MAX_VALUE);
    }

    private static String q(final String s) {
        if (s == null) return "null";
        return "\"" + escape(s) + "\"";
    }

    private static String escape(final String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String trimDouble(final double v) {
        return SimulatorAircraftJsonMapper.trim(v);
    }
}
