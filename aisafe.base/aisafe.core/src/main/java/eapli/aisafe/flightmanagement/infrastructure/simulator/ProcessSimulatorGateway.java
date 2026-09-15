package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ProcessSimulatorGateway implements SimulatorGateway {

    private static final long TIMEOUT_MINUTES = 10;

    @Override
    public SimulatorLaunchResult launch(final Path simulatorBinary,
                                        final Path workingDirectory,
                                        final Map<String, String> environment) throws SimulatorExecutionException {
        if (simulatorBinary == null || workingDirectory == null || environment == null) {
            throw new IllegalArgumentException("Simulator binary, working directory and environment are required.");
        }
        if (!Files.isRegularFile(simulatorBinary) || !Files.isExecutable(simulatorBinary)) {
            throw new SimulatorExecutionException(
                    "Flight simulator binary not found or not executable at " + simulatorBinary
                            + ". Build it with: cd flight_simulator/src/main && make");
        }
        if (!Files.isDirectory(workingDirectory)) {
            throw new SimulatorExecutionException("Simulator working directory not found: " + workingDirectory);
        }

        try {
            final ProcessBuilder builder = new ProcessBuilder(simulatorBinary.toAbsolutePath().toString());
            builder.directory(workingDirectory.toFile());
            builder.environment().putAll(environment);
            builder.redirectErrorStream(true);

            final Process process = builder.start();
            final List<String> outputLines = readOutput(process);
            if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new SimulatorExecutionException(
                        "Flight simulator timed out after " + TIMEOUT_MINUTES + " minutes.");
            }
            return new SimulatorLaunchResult(process.exitValue(), outputLines);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SimulatorExecutionException("Flight simulator execution was interrupted.", ex);
        } catch (final IOException ex) {
            throw new SimulatorExecutionException("Failed to launch flight simulator at " + simulatorBinary + ".", ex);
        }
    }

    private static List<String> readOutput(final Process process) {
        final List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ignored) {
            // process ended
        }
        return lines;
    }
}
