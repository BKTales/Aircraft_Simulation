package eapli.aisafe.enginemodelmanagement.application;

public class EngineModelAlreadyExistsException extends RuntimeException {
    public EngineModelAlreadyExistsException(final String message) {
        super(message);
    }
}

