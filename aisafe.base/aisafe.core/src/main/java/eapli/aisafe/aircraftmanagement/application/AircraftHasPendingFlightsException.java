package eapli.aisafe.aircraftmanagement.application;

public class AircraftHasPendingFlightsException extends RuntimeException {

    public AircraftHasPendingFlightsException() {
        super("Cannot decommission an aircraft that has pending flights in the system.");
    }
}
