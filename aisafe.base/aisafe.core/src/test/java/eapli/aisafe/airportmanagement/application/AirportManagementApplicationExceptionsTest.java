package eapli.aisafe.airportmanagement.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportManagementApplicationExceptionsTest {

    @Test
    void noAreaFoundMessage() {
        final var ex = new NoAreaFoundForCoordinatesException(1.0, -2.5);
        assertTrue(ex.getMessage().contains("1.0"));
        assertTrue(ex.getMessage().contains("-2.5"));
    }

    @Test
    void iataAlreadyExistsMessage() {
        final var ex = new AirportIATACodeAlreadyExistsException("LIS");
        assertTrue(ex.getMessage().contains("LIS"));
    }

    @Test
    void icaoAlreadyExistsMessage() {
        final var ex = new AirportICAOCodeAlreadyExistsException("LPPT");
        assertTrue(ex.getMessage().contains("LPPT"));
    }

    @Test
    void airControlAreaNotFoundMessage() {
        final var ex = new AirControlAreaNotFoundException("AREA-99");
        assertTrue(ex.getMessage().contains("AREA-99"));
    }
}
