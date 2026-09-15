package eapli.aisafe.app.backoffice.console.presentation.weatherdata;

import eapli.aisafe.weatherdata.application.BulkImportWeatherResult;
import eapli.aisafe.weatherdata.application.BulkWeatherDataController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("squid:S106")
public class BulkImportWeatherDataUI extends AbstractUI {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".csv");

    private static final List<Path> SEARCH_DIRS = List.of(
            Path.of(System.getProperty("user.dir")).resolve("data_weather"),
            Path.of(System.getProperty("user.dir")).resolve("aisafe.base/data_weather"),
            Path.of(System.getProperty("user.dir")).resolve("aisafe.base")
    );

    private final BulkWeatherDataController controller = new BulkWeatherDataController();

    @Override
    protected boolean doShow() {
        final List<Path> candidates = listSupportedWeatherFiles();
        if (candidates.isEmpty()) {
            System.out.println("\nNo supported weather data files found.");
            System.out.println("Place files in one of:");
            for (final Path dir : SEARCH_DIRS) {
                System.out.println("  - " + dir.toAbsolutePath());
            }
            System.out.println("Supported formats: " + String.join(", ", SUPPORTED_EXTENSIONS));
            return false;
        }

        System.out.println("\nAvailable weather data files:");
        for (int i = 0; i < candidates.size(); i++) {
            final Path file = candidates.get(i);
            System.out.printf("  %d) %s (%s)%n", i + 1, file.getFileName(), file.getParent().toAbsolutePath());
        }

        final int choice = Console.readInteger("\nChoose a file number:");
        if (choice < 1 || choice > candidates.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final Path source = candidates.get(choice - 1);
        final String confirm = Console.readLine("\nImport weather data from this file? (y/n):");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("Import cancelled.");
            return false;
        }

        final BulkImportWeatherResult result = controller.importFromFile(source);
        handleBulkImportResult(result, source.getFileName());

        return false;
    }

    private void handleBulkImportResult(final BulkImportWeatherResult result, final java.nio.file.Path fileName) {
        if (result.isSuccess()) {
            System.out.printf("%nSuccessfully imported %d weather record(s) from %s.%n",
                    result.imported().size(), fileName);
            return;
        }

        if (result.message() != null) {
            System.out.println("\nError: " + result.message());
            return;
        }

        switch (result.outcome()) {
            case UNSUPPORTED_FORMAT -> System.out.println("\nUnsupported file format.");
            case IO_ERROR -> System.out.println("\nError reading file.");
            case AREA_NOT_FOUND -> System.out.println("\nError: Area not found.");
            case SECTION_OUT_OF_BOUNDS -> System.out.println("\nError: Weather section is outside the area boundary.");
            case INVALID_HUMIDITY -> System.out.println("\nError: Invalid humidity value.");
            case INVALID_INPUT -> System.out.println("\nError: Invalid input.");
            default -> System.out.println("\nError: An unexpected error occurred.");
        }
    }

    private static List<Path> listSupportedWeatherFiles() {
        final Set<Path> unique = new LinkedHashSet<>();
        for (final Path dir : SEARCH_DIRS) {
            unique.addAll(listFilesInDirectory(dir));
        }
        final List<Path> sorted = new ArrayList<>(unique);
        sorted.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()));
        return sorted;
    }

    private static List<Path> listFilesInDirectory(final Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> hasSupportedExtension(p.getFileName().toString()))
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static boolean hasSupportedExtension(final String fileName) {
        final String lower = fileName.toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    @Override
    public String headline() {
        return "Bulk Import Weather Data (US042)";
    }
}
