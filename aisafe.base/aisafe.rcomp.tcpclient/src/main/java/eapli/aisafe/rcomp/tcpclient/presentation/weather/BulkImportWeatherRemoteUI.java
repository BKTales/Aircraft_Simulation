package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.WeatherCsvPayload;
import eapli.aisafe.rcomp.protocol.WeatherOpcodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
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
public final class BulkImportWeatherRemoteUI extends AbstractUI {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".csv");

    private static final List<Path> SEARCH_DIRS = List.of(
            Path.of(System.getProperty("user.dir")).resolve("data_weather"),
            Path.of(System.getProperty("user.dir")).resolve("aisafe.base/data_weather"),
            Path.of(System.getProperty("user.dir")).resolve("aisafe.base")
    );

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

        try {
            final byte[] content = Files.readAllBytes(source);
            final ProtocolFrame resp = RemoteTcpGateway.request(
                    WeatherOpcodes.BULK_IMPORT,
                    WeatherCsvPayload.encodeFile(source.getFileName().toString(), content));
            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                System.out.println("\n" + resp.payload());
            } else {
                System.out.println("\nImport failed: "
                        + RemoteTcpGateway.messageFrom(resp, "Request failed."));
            }
        } catch (final IOException ex) {
            System.out.println("\nError: " + ex.getMessage());
        }
        return false;
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
