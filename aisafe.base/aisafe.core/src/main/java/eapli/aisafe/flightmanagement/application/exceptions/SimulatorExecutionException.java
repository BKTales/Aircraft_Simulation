package eapli.aisafe.flightmanagement.application.exceptions;

import java.util.List;

public class SimulatorExecutionException extends RuntimeException {

    private final List<String> simulatorOutput;

    public SimulatorExecutionException(final String message) {
        this(message, List.of());
    }

    public SimulatorExecutionException(final String message, final Throwable cause) {
        this(message, cause, List.of());
    }

    public SimulatorExecutionException(final String message, final List<String> simulatorOutput) {
        super(message);
        this.simulatorOutput = simulatorOutput == null ? List.of() : List.copyOf(simulatorOutput);
    }

    public SimulatorExecutionException(final String message,
                                       final Throwable cause,
                                       final List<String> simulatorOutput) {
        super(message, cause);
        this.simulatorOutput = simulatorOutput == null ? List.of() : List.copyOf(simulatorOutput);
    }

    public List<String> simulatorOutput() {
        return simulatorOutput;
    }
}
