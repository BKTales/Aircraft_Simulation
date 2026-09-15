package eapli.aisafe.flightmanagement.application.exceptions;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoMonthlySimulationDataExceptionTest {

    @Test
    void messageIncludesAreaAndPeriod() {
        final NoMonthlySimulationDataException ex =
                new NoMonthlySimulationDataException("AREA-0", YearMonth.of(2026, 6));

        assertTrue(ex.getMessage().contains("AREA-0"));
        assertTrue(ex.getMessage().contains("2026-06"));
    }
}
