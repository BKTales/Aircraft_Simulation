package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyStatisticsAggregatorTest {

    private final MonthlyStatisticsAggregator aggregator = new MonthlyStatisticsAggregator();

    @Test
    void aggregatorComputesPassRate() {
        final List<SimulationSummaryRecord> records = List.of(
                record("2026-06-08 14:30:00", true, 5, 5, 0, 0),
                record("2026-06-10 09:15:00", false, 4, 3, 1, 3),
                record("2026-06-15 10:15:00", true, 3, 3, 0, 1));

        final MonthlyStatisticsSnapshot snapshot = aggregator.aggregate(records);

        assertEquals(3, snapshot.simulationsRun());
        assertEquals(2, snapshot.passed());
        assertEquals(1, snapshot.failed());
        assertEquals(66.7, snapshot.passRatePercent(), 0.1);
        assertEquals(12, snapshot.totalFlights());
        assertEquals(11, snapshot.successfulExecutions());
        assertEquals(1, snapshot.failedExecutions());
        assertEquals(4, snapshot.totalViolations());
        assertEquals(3, snapshot.breakdown().size());
    }

    @Test
    void rejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> aggregator.aggregate(List.of()));
        assertThrows(IllegalArgumentException.class, () -> aggregator.aggregate(null));
    }

    @Test
    void passRateIsZeroWhenNoSimulations() {
        final MonthlyStatisticsSnapshot empty = new MonthlyStatisticsSnapshot(
                0, 0, 0, 0, 0, 0, 0, List.of());
        assertEquals(0.0, empty.passRatePercent());
    }

    private static SimulationSummaryRecord record(final String generatedAt,
                                                  final boolean passed,
                                                  final int flights,
                                                  final int success,
                                                  final int failed,
                                                  final int violations) {
        return new SimulationSummaryRecord(
                LocalDateTime.parse(generatedAt.replace(' ', 'T')),
                passed,
                flights,
                success,
                failed,
                violations);
    }
}
