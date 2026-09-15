package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.flightmanagement.domain.Flight;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps simulator numeric flight IDs (hash of designator) back to IATA designators for reporting.
 */
public final class SimulatorFlightDesignatorMap {

    private SimulatorFlightDesignatorMap() {}

    public static Map<String, String> fromFlights(final List<Flight> flights) {
        final Map<String, String> map = new LinkedHashMap<>();
        if (flights == null) {
            return Map.of();
        }
        for (final Flight flight : flights) {
            if (flight == null || flight.identity() == null) {
                continue;
            }
            final String designator = flight.identity().toString();
            map.put(String.valueOf(SimulatorFlightId.fromDesignator(designator)), designator);
        }
        return Map.copyOf(map);
    }

    public static String resolve(final Map<String, String> designatorBySimulatorId, final String simulatorId) {
        if (simulatorId == null || simulatorId.isBlank()) {
            return simulatorId;
        }
        if (designatorBySimulatorId == null || designatorBySimulatorId.isEmpty()) {
            return simulatorId;
        }
        return designatorBySimulatorId.getOrDefault(simulatorId.trim(), simulatorId);
    }
}
