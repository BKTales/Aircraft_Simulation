package eapli.aisafe.aircraftmanagement.application;

public class AircraftAlreadyDecommissionedException extends RuntimeException {

    public AircraftAlreadyDecommissionedException() {
        super("This aircraft is already decommissioned.");
    }
}
