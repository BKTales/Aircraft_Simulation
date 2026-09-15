package eapli.aisafe.flightmanagement.application.exceptions;

public class AirControlAreaNotFoundException extends RuntimeException {

    public AirControlAreaNotFoundException(final String areaCode) {
        super("Air control area not found: " + areaCode);
    }
}
