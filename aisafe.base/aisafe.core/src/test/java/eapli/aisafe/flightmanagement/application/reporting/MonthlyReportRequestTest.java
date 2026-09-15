package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyReportRequestTest {

    @Test
    void exposesAllFields() {
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                1, 1, 0, 5, 5, 0, 0,
                List.of(new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                        LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 0)));
        final LocalDateTime generatedAt = LocalDateTime.of(2026, 6, 9, 12, 0);

        final MonthlyReportRequest request = new MonthlyReportRequest(
                " AREA-0 ", YearMonth.of(2026, 6), snapshot, generatedAt);

        assertEquals("AREA-0", request.areaCode());
        assertEquals(YearMonth.of(2026, 6), request.period());
        assertEquals(snapshot, request.snapshot());
        assertEquals(generatedAt, request.generatedAt());
    }

    @Test
    void defaultsGeneratedAtWhenNull() {
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                1, 1, 0, 1, 1, 0, 0, List.of());

        final MonthlyReportRequest request = new MonthlyReportRequest(
                "AREA-0", YearMonth.of(2026, 6), snapshot, null);

        assertNotNull(request.generatedAt());
    }

    @Test
    void rejectsBlankAreaCode() {
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                1, 1, 0, 1, 1, 0, 0, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> new MonthlyReportRequest("  ", YearMonth.of(2026, 6), snapshot, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new MonthlyReportRequest(null, YearMonth.of(2026, 6), snapshot, LocalDateTime.now()));
    }

    @Test
    void rejectsNullPeriodOrSnapshot() {
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                1, 1, 0, 1, 1, 0, 0, List.of());

        assertThrows(NullPointerException.class,
                () -> new MonthlyReportRequest("AREA-0", null, snapshot, LocalDateTime.now()));
        assertThrows(NullPointerException.class,
                () -> new MonthlyReportRequest("AREA-0", YearMonth.of(2026, 6), null, LocalDateTime.now()));
    }
}
