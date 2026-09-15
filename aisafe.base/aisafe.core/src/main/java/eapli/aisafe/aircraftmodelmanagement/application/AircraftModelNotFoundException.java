package eapli.aisafe.aircraftmodelmanagement.application;

public class AircraftModelNotFoundException extends RuntimeException {

    public AircraftModelNotFoundException(final String message) {
        super(message);
    }
}
