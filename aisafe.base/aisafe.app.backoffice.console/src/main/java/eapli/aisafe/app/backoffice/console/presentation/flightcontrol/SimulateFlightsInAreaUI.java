package eapli.aisafe.app.backoffice.console.presentation.flightcontrol;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.app.backoffice.console.presentation.common.ConsoleSimulationPresenter;
import eapli.aisafe.flightmanagement.application.EligibleFlightPreview;
import eapli.aisafe.flightmanagement.application.SimulateFlightsInAreaController;
import eapli.aisafe.flightmanagement.application.SimulationResult;
import eapli.aisafe.flightmanagement.application.exceptions.AirControlAreaNotFoundException;
import eapli.aisafe.flightmanagement.application.exceptions.NoEligibleFlightsException;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@SuppressWarnings("squid:S106")
public class SimulateFlightsInAreaUI extends AbstractUI {

    private final SimulateFlightsInAreaController controller = new SimulateFlightsInAreaController();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy H:mm");

    @Override
    protected boolean doShow() {
        System.out.println("\n--- Available Air Control Areas ---");
        final Iterable<AirControlArea> areas = controller.availableAreas();
        if (!areas.iterator().hasNext()) {
            System.out.println("No air control areas found.");
            return false;
        }
        for (final AirControlArea area : areas) {
            System.out.println("  " + area.identity() + " - " + area.getName().getName());
        }
        System.out.println("-----------------------------------");

        final String areaCode = Console.readLine("Air Control Area Code (e.g. AREA-0):");
        if (areaCode == null || areaCode.isBlank()) {
            System.out.println("Area code is required.");
            return false;
        }

        final LocalDateTime start;
        final LocalDateTime end;
        try {
            start = LocalDateTime.parse(Console.readLine("Start (d-M-yyyy H:mm, e.g. 26-5-2026 0:00):"), formatter);
            end = LocalDateTime.parse(Console.readLine("End (d-M-yyyy H:mm, e.g. 26-8-2026 23:59):"), formatter);
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date format. Use d-M-yyyy H:mm (e.g. 26-5-2026 0:00).");
            return false;
        }

        try {
            final List<EligibleFlightPreview> preview =
                    controller.previewEligibleFlights(areaCode.trim(), start, end);
            if (preview.isEmpty()) {
                System.out.println("\nNo eligible flights for the selected area and interval.");
                return false;
            }
            System.out.println("\nEligible flights (" + preview.size() + "):");
            for (final EligibleFlightPreview flight : preview) {
                System.out.println("  " + flight.designator() + " [" + flight.clipMode() + "]");
            }
            final String confirm = Console.readLine("Run simulation? (y/n):");
            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                return false;
            }

            ConsoleSimulationPresenter.Spinner spinner = null;
            try {
                spinner = ConsoleSimulationPresenter.startSpinner("Running simulation");
                final SimulationResult result = controller.simulateFlightsInArea(areaCode.trim(), start, end);
                spinner.stop();
                spinner = null;
                printSimulationResult(result);
            } finally {
                if (spinner != null) {
                    spinner.stop();
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("\nError: " + ex.getMessage());
        } catch (AirControlAreaNotFoundException ex) {
            System.out.println("\nError: " + ex.getMessage());
        } catch (NoEligibleFlightsException ex) {
            System.out.println("\nError: " + ex.getMessage());
        } catch (SimulatorExecutionException ex) {
            ConsoleSimulationPresenter.fail("FAIL - " + ex.getMessage());
        }
        return false;
    }

    private static void printSimulationResult(final SimulationResult result) {
        if (result.passed()) {
            ConsoleSimulationPresenter.pass("PASS");
            return;
        }
        ConsoleSimulationPresenter.fail("FAIL - " + result.failureReason());
    }

    @Override
    public String headline() {
        return "Simulate Flights in Area (US100 / US111)";
    }
}
