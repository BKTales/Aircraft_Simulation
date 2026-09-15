package eapli.aisafe.aircraftmodelmanagement.application;

public class AircraftModelAlreadyExistsException extends RuntimeException {
    public AircraftModelAlreadyExistsException(final String message) {
        super(message);
    }
}
