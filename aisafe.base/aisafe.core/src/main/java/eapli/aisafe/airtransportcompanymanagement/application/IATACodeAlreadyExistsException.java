package eapli.aisafe.airtransportcompanymanagement.application;

public class IATACodeAlreadyExistsException extends IllegalArgumentException {
    public IATACodeAlreadyExistsException(final String iataCode) {
        super("IATA code already in use: " + iataCode);
    }
}
