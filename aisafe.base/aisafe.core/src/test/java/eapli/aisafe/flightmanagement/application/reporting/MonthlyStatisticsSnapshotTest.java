package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyStatisticsSnapshotTest {

    @Test
    void exposesAggregatedValues() {
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                2, 1, 1, 9, 8, 1, 3,
                List.of(new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                        LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 0)));

        assertEquals(2, snapshot.simulationsRun());
        assertEquals(1, snapshot.passed());
        assertEquals(1, snapshot.failed());
        assertEquals(50.0, snapshot.passRatePercent());
        assertEquals(9, snapshot.totalFlights());
        assertEquals(8, snapshot.successfulExecutions());
        assertEquals(1, snapshot.failedExecutions());
        assertEquals(3, snapshot.totalViolations());
        assertEquals(1, snapshot.breakdown().size());
    }

    @Test
    void breakdownRowExposesFields() {
        final LocalDateTime at = LocalDateTime.of(2026, 6, 10, 9, 15);
        final MonthlyStatisticsSnapshot.SimulationBreakdownRow row =
                new MonthlyStatisticsSnapshot.SimulationBreakdownRow(at, false, 4, 3);

        assertEquals(at, row.dateTime());
        assertEquals(false, row.passed());
        assertEquals(4, row.flights());
        assertEquals(3, row.violations());
    }

    @Test
    void rejectsNullBreakdown() {
        assertThrows(NullPointerException.class,
                () -> new MonthlyStatisticsSnapshot(0, 0, 0, 0, 0, 0, 0, null));
    }
}
