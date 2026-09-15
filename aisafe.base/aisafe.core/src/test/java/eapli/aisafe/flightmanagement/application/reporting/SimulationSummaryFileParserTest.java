package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationSummaryFileParserTest {

    private final SimulationSummaryFileParser parser = new SimulationSummaryFileParser();

    @Test
    void parserReadsPassSummary() {
        final SimulationSummaryRecord record = parser.parse(
                Us112SummaryFixtures.passSummary("2026-06-08 14:30:00"));

        assertEquals(LocalDateTime.of(2026, 6, 8, 14, 30), record.generatedAt());
        assertTrue(record.passed());
        assertEquals(5, record.totalFlights());
        assertEquals(5, record.successfulExecutions());
        assertEquals(0, record.failedExecutions());
        assertEquals(0, record.violations());
    }

    @Test
    void parserReadsFailSummaryWithViolations() {
        final SimulationSummaryRecord record = parser.parse(
                Us112SummaryFixtures.failSummary("2026-06-10 09:15:00"));

        assertFalse(record.passed());
        assertEquals(4, record.totalFlights());
        assertEquals(3, record.successfulExecutions());
        assertEquals(1, record.failedExecutions());
        assertEquals(3, record.violations());
    }

    @Test
    void rejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
    }

    @Test
    void rejectsMissingGeneratedAt() {
        final String content = """
                FINAL RESULT      : PASS
                Total flights     : 1
                """;

        assertThrows(IllegalArgumentException.class, () -> parser.parse(content));
    }

    @Test
    void rejectsMissingFinalResult() {
        final String content = """
                Generated at      : 2026-06-08 14:30:00
                Total flights     : 1
                """;

        assertThrows(IllegalArgumentException.class, () -> parser.parse(content));
    }

    @Test
    void rejectsInvalidGeneratedAtTimestamp() {
        final String content = """
                Generated at      : not-a-date
                FINAL RESULT      : PASS
                """;

        assertThrows(IllegalArgumentException.class, () -> parser.parse(content));
    }

    @Test
    void treatsNonNumericCountsAsZero() {
        final String content = """
                Generated at      : 2026-06-08 14:30:00
                FINAL RESULT      : PASS
                Total flights     : x
                Completed (SUCCESS): y
                Failed execution  : z
                SAFETY VIOLATIONS (n)
                """;

        final SimulationSummaryRecord record = parser.parse(content);

        assertEquals(0, record.totalFlights());
        assertEquals(0, record.successfulExecutions());
        assertEquals(0, record.failedExecutions());
        assertEquals(0, record.violations());
    }

    @Test
    void skipsEmptyLines() {
        final String content = """

                Generated at      : 2026-06-08 14:30:00

                FINAL RESULT      : FAIL
                Total flights     : 2
                """;

        final SimulationSummaryRecord record = parser.parse(content);

        assertFalse(record.passed());
        assertEquals(2, record.totalFlights());
    }
}
