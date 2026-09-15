package eapli.aisafe.aircraftmanagement.application;

public class AircraftNotInCompanyFleetException extends RuntimeException {

    public AircraftNotInCompanyFleetException() {
        super("That aircraft is not in your company's fleet.");
    }
}
