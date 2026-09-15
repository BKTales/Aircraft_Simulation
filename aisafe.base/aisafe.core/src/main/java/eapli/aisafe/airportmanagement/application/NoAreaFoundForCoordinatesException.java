package eapli.aisafe.airportmanagement.application;

/**
 * Thrown when no registered air control area contains the given airport coordinates.
 *
 * @author aisafe team
 */
public class NoAreaFoundForCoordinatesException extends RuntimeException {

    public NoAreaFoundForCoordinatesException(final double latitude, final double longitude) {
        super("No air control area contains the coordinates (" + latitude + ", " + longitude + ").");
    }
}
