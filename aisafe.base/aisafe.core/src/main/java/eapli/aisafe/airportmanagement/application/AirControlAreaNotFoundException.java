package eapli.aisafe.airportmanagement.application;

/**
 * Thrown when the specified air control area does not exist in the system.
 *
 * @author aisafe team
 */
public class AirControlAreaNotFoundException extends RuntimeException {

    public AirControlAreaNotFoundException(final String areaCode) {
        super("Air control area '" + areaCode + "' does not exist.");
    }
}
