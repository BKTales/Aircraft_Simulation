package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.aisafe.app.backoffice.console.presentation.common.ConsoleSimulationPresenter;
import eapli.aisafe.dsl.api.FlightPlanParseErrorReporter;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.ValidateFlightPlanDslFailurePayload;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class ValidateFlightPlanRemoteUI extends AbstractUI {

    private static final String DRAFT = "DRAFT";

    @Override
    protected boolean doShow() {
        try {
            final FlightRow selected = chooseFlight();
            if (selected == null) {
                return false;
            }
            printContextCard(selected);

            final String confirm = Console.readLine(
                    "\nValidate flight plan " + selected.designator + "? (y/n):");
            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return false;
            }

            ConsoleSimulationPresenter.step(1, 3, "Check flight plan syntax");
            ConsoleSimulationPresenter.step(2, 3, "Run safety simulation");
            final ConsoleSimulationPresenter.Spinner spinner =
                    ConsoleSimulationPresenter.startSpinner("Running validation");
            ProtocolFrame response;
            try {
                response = RemoteTcpGateway.request(PilotOpcodes.VALIDATE_FLIGHT_PLAN, selected.designator);
            } finally {
                spinner.stop();
            }
            printValidationResponse(response, selected.designator);
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private FlightRow chooseFlight() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(PilotOpcodes.LIST_MY_FLIGHTS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<FlightRow> draftFlights = new ArrayList<>();
        for (final String line : resp.payload().split("\n")) {
            final FlightRow row = FlightRow.parse(line);
            if (row != null && DRAFT.equalsIgnoreCase(row.status)) {
                draftFlights.add(row);
            }
        }
        if (draftFlights.isEmpty()) {
            System.out.println("No DRAFT flight plans assigned to you.");
            return null;
        }
        System.out.println("\nSelect a DRAFT flight to validate:");
        for (int i = 0; i < draftFlights.size(); i++) {
            final FlightRow row = draftFlights.get(i);
            System.out.printf("  %d. %-10s [%s] %s%n", i + 1, row.designator, row.status, row.route);
        }
        final int choice = Console.readInteger("\nPlease choose an option:");
        if (choice <= 0 || choice > draftFlights.size()) {
            System.out.println("Operation cancelled.");
            return null;
        }
        return draftFlights.get(choice - 1);
    }

    private static void printContextCard(final FlightRow flight) {
        System.out.println("\n--- Selected flight ---");
        System.out.println("  Flight:   " + flight.designator);
        System.out.println("  Route:    " + (flight.route.isBlank() ? "-" : flight.route));
        System.out.println("  Aircraft: " + (flight.aircraft.isBlank() ? "-" : flight.aircraft));
        System.out.println("  Status:   " + flight.status);
    }

    private static void printValidationResponse(final ProtocolFrame response, final String designator) {
        if (response == null) {
            System.out.println("No response from server.");
            return;
        }
        if (response.opcode() == ResponseCodes.BAD_REQUEST
                && ValidateFlightPlanDslFailurePayload.matches(response.payload())) {
            final ValidateFlightPlanDslFailurePayload.Decoded decoded =
                    ValidateFlightPlanDslFailurePayload.decode(response.payload());
            ConsoleSimulationPresenter.step(1, 3, "Check flight plan syntax");
            ConsoleSimulationPresenter.fail("  FAILED");
            FlightPlanParseErrorReporter.print(System.out, decoded.dslContent(), decoded.errors());
            ConsoleSimulationPresenter.warn("Simulation was NOT started.");
            return;
        }
        if (response.opcode() == ResponseCodes.BAD_REQUEST) {
            ConsoleSimulationPresenter.fail("Validation blocked: " + response.payload());
            return;
        }
        if (response.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(response);
            return;
        }
        final String[] parts = response.payload().split("\\|", 4);
        ConsoleSimulationPresenter.step(3, 3, "Result");
        if (parts.length >= 3 && "PASS".equals(parts[1])) {
            ConsoleSimulationPresenter.pass("  APPROVED - flight plan [" + parts[2] + "]");
            return;
        }
        if (parts.length >= 4 && "FAIL".equals(parts[1])) {
            ConsoleSimulationPresenter.fail("  REJECTED - flight plan [" + parts[2] + "]: " + parts[3]);
            return;
        }
        RemoteTcpGateway.printResponse(response);
    }

    @Override
    public String headline() {
        return "Validate flight plan (US085)";
    }

    /** One flight from LIST_MY_FLIGHTS: {@code designator|route|aircraft|status|weather}. */
    private static final class FlightRow {
        private final String designator;
        private final String route;
        private final String aircraft;
        private final String status;

        private FlightRow(final String designator, final String route,
                          final String aircraft, final String status) {
            this.designator = designator;
            this.route = route;
            this.aircraft = aircraft;
            this.status = status;
        }

        static FlightRow parse(final String line) {
            if (line == null || line.isBlank()) {
                return null;
            }
            final String[] parts = line.trim().split("\\|", -1);
            if (parts.length < 1 || parts[0].isBlank()) {
                return null;
            }
            return new FlightRow(
                    parts[0].trim(),
                    parts.length > 1 ? parts[1].trim() : "",
                    parts.length > 2 ? parts[2].trim() : "",
                    parts.length > 3 ? parts[3].trim() : "");
        }
    }
}
