package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.aisafe.dsl.api.FlightPlanParseErrorReporter;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.flightmanagement.application.ImportFlightPlanFromFileController;
import eapli.aisafe.flightmanagement.application.ImportFlightPlanResult;
import eapli.aisafe.flightmanagement.application.importfile.FlightPlanFileExtensions;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("squid:S106")
public class ImportFlightPlanFromFileUI extends AbstractUI {

    private static final Path DEFAULT_DIR =
            Path.of(System.getProperty("user.dir")).resolve("flightplans");

    private final ImportFlightPlanFromFileController controller = new ImportFlightPlanFromFileController();

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

        final ParseResult result = controller.parseFile(source);
        if (!result.isValid()) {
            FlightPlanParseErrorReporter.print(System.out, source, result.getErrors());
            return false;
        }

        final var descriptor = result.getDescriptor();
        System.out.println("\nFlight plan is valid.");
        System.out.println("  Flight id: " + descriptor.getFlightId());
        System.out.println("  Type:      " + descriptor.getFlightType());
        System.out.println("  Legs:      " + descriptor.getLegs().size());

        final List<String> aircraftRegs = controller.activeAircraftRegistrations();
        if (aircraftRegs.isEmpty()) {
            System.out.println("\nNo active aircraft registered. Cannot import.");
            return false;
        }

        System.out.println("\nActive aircraft:");
        for (int i = 0; i < aircraftRegs.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + aircraftRegs.get(i));
        }

        final int aircraftChoice = Console.readInteger("\nChoose aircraft registration:");
        if (aircraftChoice < 1 || aircraftChoice > aircraftRegs.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final String aircraftReg = aircraftRegs.get(aircraftChoice - 1);
        final String confirm = Console.readLine("\nImport flight into the system? (y/n):");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("Import cancelled.");
            return false;
        }

        final ImportFlightPlanResult imported = controller.importValidPlan(source, aircraftReg);
        if (imported.isSuccess()) {
            System.out.println("\n" + imported.message());
        } else {
            System.out.println("\nImport failed: " + imported.message());
        }

        return false;
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
        return "Import flight plan from file (US121)";
    }
}
