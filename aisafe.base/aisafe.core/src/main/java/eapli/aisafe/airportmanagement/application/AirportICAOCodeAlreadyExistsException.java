package eapli.aisafe.airportmanagement.application;

/**
 * Thrown when attempting to register an airport whose ICAO code is already in use.
 *
 * @author aisafe team
 */
public class AirportICAOCodeAlreadyExistsException extends RuntimeException {

    public AirportICAOCodeAlreadyExistsException(final String icaoCode) {
        super("An airport with ICAO code '" + icaoCode + "' already exists.");
    }
}
