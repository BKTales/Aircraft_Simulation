package eapli.aisafe.aircraftmanagement.application;

public class AircraftNotFoundException extends RuntimeException {

    public AircraftNotFoundException(final String message) {
        super(message);
    }
}
