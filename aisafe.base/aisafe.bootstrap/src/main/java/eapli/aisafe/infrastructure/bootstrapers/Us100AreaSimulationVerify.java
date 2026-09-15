package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.flightmanagement.application.EligibleFlightPreview;
import eapli.aisafe.flightmanagement.application.FlightSimulationService;
import eapli.aisafe.flightmanagement.application.SimulationResult;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prints US100 eligible-flight preview for bootstrap data (manual acceptance helper).
 */
public final class Us100AreaSimulationVerify {

    private Us100AreaSimulationVerify() {}

    public static void main(final String[] args) throws Exception {
        final var repos = PersistenceContext.repositories();
        final FlightSimulationService service = new FlightSimulationService(
                repos.flights(),
                repos.airControlArea(),
                repos.aircraft(),
                repos.aircraftModels(),
                repos.engineModels(),
                repos.airports(),
                repos.weatherData());

        preview(service, "US085/demo window", FlightBootstrapper.nextTp1001Departure().minusHours(1), 4);
        preview(service, "US100 PASS window", FlightBootstrapper.nextUs100PassDeparture().minusHours(1), 4);

        if (args.length > 0 && "run-pass".equals(args[0])) {
            final LocalDateTime start = FlightBootstrapper.nextUs100PassDeparture().minusHours(1);
            final LocalDateTime end = start.plusHours(3);
            final SimulationResult result = service.simulateFlightsInArea("AREA-0", start, end);
            System.out.println("US100 PASS simulation: " + (result.passed() ? "PASS" : "FAIL")
                    + ", exported=" + result.exportedPlans());
        }
    }

    private static void preview(final FlightSimulationService service,
                                final String label,
                                final LocalDateTime start,
                                final int hours) {
        final LocalDateTime end = start.plusHours(hours);
        final List<EligibleFlightPreview> preview = service.listEligibleForArea("AREA-0", start, end);
        System.out.println(label + " AREA-0 " + start + " .. " + end);
        if (preview.isEmpty()) {
            System.out.println("  (no eligible flights)");
            return;
        }
        for (final EligibleFlightPreview flight : preview) {
            System.out.println("  " + flight.designator() + " [" + flight.clipMode() + "]");
        }
    }
}
