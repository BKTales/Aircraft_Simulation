package eapli.aisafe.dsl.api;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record SimulatorFlightPlanJsonContext(
        String aircraftId,
        AircraftModel aircraftModel,
        EngineModel engineModel,
        Map<String, Airport> airportsByIata) {

    public SimulatorFlightPlanJsonContext {
        Objects.requireNonNull(aircraftId, "aircraftId");
        Objects.requireNonNull(aircraftModel, "aircraftModel");
        Objects.requireNonNull(engineModel, "engineModel");
        airportsByIata = airportsByIata == null ? Map.of() : Map.copyOf(airportsByIata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String aircraftId;
        private AircraftModel aircraftModel;
        private EngineModel engineModel;
        private final Map<String, Airport> airports = new HashMap<>();

        public Builder aircraftId(final String id) {
            this.aircraftId = id;
            return this;
        }

        public Builder aircraftModel(final AircraftModel model) {
            this.aircraftModel = model;
            return this;
        }

        public Builder engineModel(final EngineModel engine) {
            this.engineModel = engine;
            return this;
        }

        public Builder airport(final Airport airport) {
            airports.put(airport.identity().toString(), airport);
            airports.put(airport.icaoCode().toString(), airport);
            return this;
        }

        public Builder airports(final Map<String, Airport> map) {
            if (map != null) {
                airports.putAll(map);
            }
            return this;
        }

        public SimulatorFlightPlanJsonContext build() {
            return new SimulatorFlightPlanJsonContext(aircraftId, aircraftModel, engineModel, airports);
        }
    }
}
