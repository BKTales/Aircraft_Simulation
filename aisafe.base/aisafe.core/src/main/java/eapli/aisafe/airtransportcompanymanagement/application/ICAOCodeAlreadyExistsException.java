package eapli.aisafe.airtransportcompanymanagement.application;

public class ICAOCodeAlreadyExistsException extends IllegalArgumentException {
    public ICAOCodeAlreadyExistsException(final String icaoCode) {
        super("ICAO code already in use: " + icaoCode);
    }
}
