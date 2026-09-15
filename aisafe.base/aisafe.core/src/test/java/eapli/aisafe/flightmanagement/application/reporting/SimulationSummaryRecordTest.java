package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationSummaryRecordTest {

    @Test
    void exposesAllFields() {
        final LocalDateTime at = LocalDateTime.of(2026, 6, 8, 14, 30);
        final SimulationSummaryRecord record = new SimulationSummaryRecord(at, true, 5, 4, 1, 2);

        assertEquals(at, record.generatedAt());
        assertTrue(record.passed());
        assertEquals(5, record.totalFlights());
        assertEquals(4, record.successfulExecutions());
        assertEquals(1, record.failedExecutions());
        assertEquals(2, record.violations());
    }

    @Test
    void rejectsNullGeneratedAt() {
        assertThrows(NullPointerException.class,
                () -> new SimulationSummaryRecord(null, false, 0, 0, 0, 0));
    }

    @Test
    void tracksFailResult() {
        final SimulationSummaryRecord record = new SimulationSummaryRecord(
                LocalDateTime.now(), false, 1, 0, 1, 3);
        assertFalse(record.passed());
    }
}
