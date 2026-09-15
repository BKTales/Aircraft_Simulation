package eapli.aisafe.airtransportcompanymanagement.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AirTransportCompanyApplicationExceptionsTest {

    @Test
    void iataCodeAlreadyExistsMessageContainsCode() {
        final var ex = new IATACodeAlreadyExistsException("XX");
        assertTrue(ex.getMessage().contains("XX"));
    }

    @Test
    void icaoCodeAlreadyExistsMessageContainsCode() {
        final var ex = new ICAOCodeAlreadyExistsException("YYY");
        assertTrue(ex.getMessage().contains("YYY"));
    }
}
