package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.aisafe.dsl.api.FlightPlanParseErrorReporter;
import eapli.aisafe.flightmanagement.application.importfile.FlightPlanFileExtensions;
import eapli.aisafe.rcomp.protocol.PilotFlightPlanPayload;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * US081 / US121 — same flow as backoffice {@code ImportFlightPlanFromFileUI}, over TCP.
 */
@SuppressWarnings("squid:S106")
public final class ImportFlightPlanRemoteUI extends AbstractUI {

    private static final Path DEFAULT_DIR =
            Path.of(System.getProperty("user.dir")).resolve("flightplans");

    @Override
    protected boolean doShow() {
        final List<Path> candidates = listFlightPlanFiles(DEFAULT_DIR);
        if (candidates.isEmpty()) {
            System.out.println("\nNo flight plan files found in: " + DEFAULT_DIR.toAbsolutePath());
            System.out.println("Supported formats: .txt, .dsl, or no extension (Core Flight DSL).");
            System.out.println("Create the folder and add flight plan files there.");
            return false;
        }

        System.out.println("\nAvailable flight plan files in: " + DEFAULT_DIR.toAbsolutePath());
        for (int i = 0; i < candidates.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + candidates.get(i).getFileName());
        }

        final int choice = Console.readInteger("\nChoose a file number:");
        if (choice < 1 || choice > candidates.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final Path source = candidates.get(choice - 1);
        final byte[] content;
        try {
            content = Files.readAllBytes(source);
        } catch (final IOException ex) {
            System.out.println("Could not read file: " + ex.getMessage());
            return false;
        }

        try {
            final ProtocolFrame parseResp = RemoteTcpGateway.request(
                    PilotOpcodes.PARSE_FLIGHT_PLAN_FILE,
                    PilotFlightPlanPayload.encodeFile(source.getFileName().toString(), content));
            if (parseResp == null || parseResp.opcode() != ResponseCodes.OK) {
                if (parseResp != null && parseResp.opcode() == ResponseCodes.BAD_REQUEST) {
                    FlightPlanParseErrorReporter.print(
                            System.out, source, Arrays.asList(parseResp.payload().split("\n")));
                } else {
                    RemoteTcpGateway.printResponse(parseResp);
                }
                return false;
            }
            printParseSummary(parseResp.payload());

            final String aircraftReg = chooseAircraft();
            if (aircraftReg == null) {
                return false;
            }

            final String confirm = Console.readLine("\nImport flight into the system? (y/n):");
            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                System.out.println("Import cancelled.");
                return false;
            }

            final ProtocolFrame importResp = RemoteTcpGateway.request(
                    PilotOpcodes.IMPORT_FLIGHT_PLAN,
                    PilotFlightPlanPayload.encodeImport(source.getFileName().toString(), content, aircraftReg));
            if (importResp != null && importResp.opcode() == ResponseCodes.OK) {
                System.out.println("\n" + importResp.payload());
            } else {
                System.out.println("\nImport failed: "
                        + RemoteTcpGateway.messageFrom(importResp, "Request failed."));
            }
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private static void printParseSummary(final String payload) {
        final String[] parts = payload.split("\\|", -1);
        if (parts.length >= 4 && "VALID".equals(parts[0])) {
            System.out.println("\nFlight plan is valid.");
            System.out.println("  Flight id: " + parts[1]);
            System.out.println("  Type:      " + parts[2]);
            System.out.println("  Legs:      " + parts[3]);
        } else {
            System.out.println(payload);
        }
    }

    private String chooseAircraft() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(PilotOpcodes.LIST_IMPORT_AIRCRAFT, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> regs = Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().toList();
        if (regs.isEmpty()) {
            System.out.println("\nNo active aircraft registered. Cannot import.");
            return null;
        }
        System.out.println("\nActive aircraft:");
        final SelectWidget<String> selector = new SelectWidget<>("", regs);
        selector.show();
        return selector.selectedElement();
    }

    private static List<Path> listFlightPlanFiles(final Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .filter(p -> FlightPlanFileExtensions.isSupported(
                            FlightPlanFileExtensions.normalizeExtension(p)))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public String headline() {
        return "Import flight plan from file (US081/US121)";
    }
}
