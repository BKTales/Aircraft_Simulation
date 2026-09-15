package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;

import java.nio.file.Path;
import java.util.Map;

/**
 * Port for launching the external C flight simulator process.
 */
public interface SimulatorGateway {

    SimulatorLaunchResult launch(Path simulatorBinary, Path workingDirectory, Map<String, String> environment)
            throws SimulatorExecutionException;
}
