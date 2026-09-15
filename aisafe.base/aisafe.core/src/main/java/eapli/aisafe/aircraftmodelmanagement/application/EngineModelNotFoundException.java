package eapli.aisafe.aircraftmodelmanagement.application;

public class EngineModelNotFoundException extends RuntimeException {
    public EngineModelNotFoundException(final String message) {
        super(message);
    }
}
