package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.dsl.api.FlightPlanJsonExporter;
import eapli.aisafe.dsl.api.SimulatorFlightPlanJsonContext;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.flightmanagement.infrastructure.simulator.Lapr4FlightPlanSimulatorEnricher;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;

import java.util.HashMap;
import java.util.Map;

public final class SimulatorFlightPlanJsonBuilder {

    private final AircraftModelRepository aircraftModels;
    private final EngineModelRepository engineModels;
    private final AirportRepository airports;

    public SimulatorFlightPlanJsonBuilder(final AircraftModelRepository aircraftModels,
                                            final EngineModelRepository engineModels,
                                            final AirportRepository airports) {
        if (aircraftModels == null || engineModels == null || airports == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.aircraftModels = aircraftModels;
        this.engineModels = engineModels;
        this.airports = airports;
    }

    public String toSimulatorJson(final FlightPlanDescriptor descriptor, final Aircraft aircraft) {
        final AircraftModel model = aircraftModels.ofIdentity(aircraft.aircraftModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown aircraft model: " + aircraft.aircraftModelId()));
        final EngineModel engine = engineModels.findByModelId(aircraft.engineModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown engine model: " + aircraft.engineModelId()));

        final Map<String, Airport> airportMap = resolveAirports(descriptor);
        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId(aircraft.aircraftModelId().toString())
                .aircraftModel(model)
                .engineModel(engine)
                .airports(airportMap)
                .build();
        return FlightPlanJsonExporter.toSimulatorJson(descriptor, context);
    }

    public String toSimulatorJson(final FlightPlanDescriptor descriptor,
                                  final Aircraft aircraft,
                                  final Airport departure,
                                  final Airport arrival) {
        final AircraftModel model = aircraftModels.ofIdentity(aircraft.aircraftModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown aircraft model: " + aircraft.aircraftModelId()));
        final EngineModel engine = engineModels.findByModelId(aircraft.engineModelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown engine model: " + aircraft.engineModelId()));

        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId(aircraft.aircraftModelId().toString())
                .aircraftModel(model)
                .engineModel(engine)
                .airport(departure)
                .airport(arrival)
                .build();
        return FlightPlanJsonExporter.toSimulatorJson(descriptor, context);
    }

    public boolean isSelfContained(final String jsonContent) {
        if (jsonContent == null || jsonContent.contains("\"Leg\": []")) {
            return false;
        }
        return jsonContent.contains("\"Aircraft\"")
                && jsonContent.contains("\"DepartureAirport\"")
                && jsonContent.contains("\"ModelId\"");
    }

    public String enrichLapr4ForSimulator(final String lapr4Json,
                                          final Aircraft aircraft,
                                          final FlightPlanDescriptor descriptor) {
        return new Lapr4FlightPlanSimulatorEnricher(aircraftModels, engineModels, this)
                .enrich(lapr4Json, aircraft, descriptor);
    }

    private Map<String, Airport> resolveAirports(final FlightPlanDescriptor descriptor) {
        final Map<String, Airport> map = new HashMap<>();
        for (final LegDescriptor leg : descriptor.getLegs()) {
            putAirport(map, leg.getDepartureAirport());
            putAirport(map, leg.getArrivalAirport());
        }
        return map;
    }

    private void putAirport(final Map<String, Airport> map, final String code) {
        final Airport airport = requireAirport(code);
        map.put(airport.identity().toString(), airport);
        map.put(airport.icaoCode().toString(), airport);
        map.put(code, airport);
    }

    public Airport requireAirportForCode(final String code) {
        return requireAirport(code);
    }

    private Airport requireAirport(final String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Airport code is required.");
        }
        if (code.length() == 3) {
            return airports.ofIdentity(AirportIATACode.valueOf(code))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown airport IATA code: " + code));
        }
        return airports.findByIcaoCode(AirportICAOCode.valueOf(code))
                .orElseThrow(() -> new IllegalArgumentException("Unknown airport ICAO code: " + code));
    }
}
