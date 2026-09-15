package eapli.aisafe.airportmanagement.application;

/**
 * Thrown when attempting to register an airport whose IATA code is already in use.
 *
 * @author aisafe team
 */
public class AirportIATACodeAlreadyExistsException extends RuntimeException {

    public AirportIATACodeAlreadyExistsException(final String iataCode) {
        super("An airport with IATA code '" + iataCode + "' already exists.");
    }
}
