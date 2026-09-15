package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.aisafe.app.backoffice.console.presentation.common.ConsoleSimulationPresenter;
import eapli.aisafe.dsl.api.FlightPlanParseErrorReporter;
import eapli.aisafe.flightmanagement.application.FlightValidationPreview;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanController;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanResult;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("squid:S106")
public final class ValidateFlightPlanUI extends AbstractUI {

    private static final DateTimeFormatter SCHEDULE = DateTimeFormatter.ofPattern("d-M-yyyy H:mm");

    private final ValidateFlightPlanController controller = new ValidateFlightPlanController();

    @Override
    protected boolean doShow() {
        final List<FlightValidationPreview> candidates = controller.listValidatableFlights();
        final FlightValidationPreview selected = chooseFlight(candidates);
        final String designator = selected != null ? selected.designator() : readManualDesignator();
        if (designator == null || designator.isBlank()) {
            System.out.println("Operation cancelled.");
            return false;
        }

        if (selected != null) {
            printContextCard(selected);
        }

        final String confirm = Console.readLine("\nValidate flight plan " + designator.trim() + "? (y/n):");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("Operation cancelled.");
            return false;
        }

        runValidation(designator.trim(), selected);
        return false;
    }

    private void runValidation(final String designator, final FlightValidationPreview selected) {
        final AtomicReference<ConsoleSimulationPresenter.Spinner> spinner = new AtomicReference<>();
        try {
            final ValidateFlightPlanResult result = controller.validateFlightPlan(designator, () -> {
                ConsoleSimulationPresenter.step(1, 3, "Check flight plan syntax");
                ConsoleSimulationPresenter.ok("OK");
                ConsoleSimulationPresenter.step(2, 3, "Run safety simulation");
                spinner.set(ConsoleSimulationPresenter.startSpinner("Running simulation"));
            });
            stopSpinner(spinner);
            printResult(result, selected);
        } finally {
            stopSpinner(spinner);
        }
    }

    private static void stopSpinner(final AtomicReference<ConsoleSimulationPresenter.Spinner> spinner) {
        final ConsoleSimulationPresenter.Spinner running = spinner.getAndSet(null);
        if (running != null) {
            running.stop();
        }
    }

    private FlightValidationPreview chooseFlight(final List<FlightValidationPreview> candidates) {
        if (candidates.isEmpty()) {
            System.out.println("\nNo DRAFT flight plans to validate. Enter a designator manually.");
            return null;
        }
        System.out.println("\nSelect a DRAFT flight to validate:");
        for (int i = 0; i < candidates.size(); i++) {
            final FlightValidationPreview flight = candidates.get(i);
            System.out.printf("  %d. %-10s [%s] %s%n",
                    i + 1, flight.designator(), flight.status(), flight.routeLabel());
        }
        System.out.println("  0. Enter designator manually");

        final int choice = Console.readInteger("\nPlease choose an option:");
        if (choice <= 0 || choice > candidates.size()) {
            return null;
        }
        return candidates.get(choice - 1);
    }

    private static String readManualDesignator() {
        return Console.readLine("Flight designator (e.g., TP1234):");
    }

    private static void printContextCard(final FlightValidationPreview flight) {
        System.out.println("\n--- Selected flight ---");
        System.out.println("  Flight:    " + flight.designator());
        System.out.println("  Route:     " + flight.routeLabel());
        System.out.println("  Aircraft:  " + flight.aircraftRegistration());
        System.out.println("  Status:    " + flight.status());
        if (flight.scheduledDeparture() != null) {
            System.out.println("  Departure: " + SCHEDULE.format(flight.scheduledDeparture()));
        }
    }

    private static void printResult(final ValidateFlightPlanResult result, final FlightValidationPreview selected) {
        if (result.dslFailure()) {
            ConsoleSimulationPresenter.step(1, 3, "Check flight plan syntax");
            ConsoleSimulationPresenter.fail("  FAILED");
            printDslErrors(result, selected);
            ConsoleSimulationPresenter.warn("Simulation was NOT started.");
            ConsoleSimulationPresenter.info("Status unchanged: " + result.status());
            return;
        }

        if (result.status() == FlightPlanStatus.DRAFT) {
            ConsoleSimulationPresenter.fail("REJECTED - flight plan NOT approved.");
            if (result.message() != null && !result.message().isBlank()) {
                ConsoleSimulationPresenter.fail("Reason: " + result.message());
            }
            return;
        }

        ConsoleSimulationPresenter.step(3, 3, "Result");
        if (result.passed()) {
            ConsoleSimulationPresenter.pass("  APPROVED - flight plan [" + result.flightDesignator() + "]");
            ConsoleSimulationPresenter.ok("Status saved: " + result.status());
            return;
        }
        ConsoleSimulationPresenter.fail("  REJECTED - flight plan [" + result.flightDesignator() + "]: " + reason(result));
        ConsoleSimulationPresenter.fail("Status saved: " + result.status());
    }

    private static void printDslErrors(final ValidateFlightPlanResult result, final FlightValidationPreview selected) {
        final String dsl = result.dslContent();
        if (dsl != null && !dsl.isBlank()) {
            FlightPlanParseErrorReporter.print(System.out, dsl, result.errors());
            return;
        }
        for (final String error : result.errors()) {
            System.out.println("  [ERROR] " + error);
        }
    }

    private static String reason(final ValidateFlightPlanResult result) {
        return Optional.ofNullable(result.message())
                .filter(m -> !m.isBlank())
                .orElse("Simulation failed.");
    }

    @Override
    public String headline() {
        return "Validate flight plan (US085)";
    }
}
