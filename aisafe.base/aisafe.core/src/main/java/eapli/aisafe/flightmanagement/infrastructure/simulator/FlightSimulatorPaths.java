package eapli.aisafe.flightmanagement.infrastructure.simulator;

import java.nio.file.Path;

/**
 * Resolves filesystem paths to the bundled C flight_simulator artefacts.
 */
public final class FlightSimulatorPaths {

    private final Path projectRoot;

    public FlightSimulatorPaths() {
        this(Path.of(System.getProperty("user.dir", ".")));
    }

    public FlightSimulatorPaths(final Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public Path simulatorBinary() {
        return resolve("flight_simulator/src/main/flight_simulator");
    }

    public Path simulatorWorkingDirectory() {
        return resolve("flight_simulator/src/main");
    }



    private Path resolve(final String relative) {
        Path current = projectRoot.toAbsolutePath().normalize();
        for (int depth = 0; depth < 5 && current != null; depth++) {
            final Path candidate = current.resolve(relative).normalize();
            if (candidate.toFile().exists()) {
                return candidate;
            }
            current = current.getParent();
        }
        return projectRoot.resolve(relative).toAbsolutePath().normalize();
    }
}
