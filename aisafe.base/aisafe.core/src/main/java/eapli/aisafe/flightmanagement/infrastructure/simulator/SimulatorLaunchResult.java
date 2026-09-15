package eapli.aisafe.flightmanagement.infrastructure.simulator;

import java.util.List;

public record SimulatorLaunchResult(int exitCode, List<String> outputLines) {

    public SimulatorLaunchResult {
        outputLines = outputLines == null ? List.of() : List.copyOf(outputLines);
    }
}
