package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.infrastructure.reporting.MonthlyReportsPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class MonthlyStatisticsReportGeneratorTest {

    @Test
    void generatorWritesBrandedHeader(@TempDir final Path tempDir) throws Exception {
        final MonthlyStatisticsReportGenerator generator =
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(tempDir));
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                2, 1, 1, 9, 8, 1, 3,
                List.of(
                        new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                                LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 0),
                        new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                                LocalDateTime.of(2026, 6, 10, 9, 15), false, 4, 3)));

        final MonthlyReportResult result = generator.generate(new MonthlyReportRequest(
                "AREA-0",
                YearMonth.of(2026, 6),
                snapshot,
                LocalDateTime.of(2026, 6, 9, 12, 0)));

        assertTrue(Files.isRegularFile(result.reportPath()));
        assertTrue(result.formattedContent().contains("AISafe MONTHLY STATISTICS REPORT"));
        assertTrue(result.formattedContent().contains("PASS/FAIL CHART (ASCII)"));
        assertTrue(result.formattedContent().contains("50.0%"));
    }

    @Test
    void generatorOverwritesSameMonth(@TempDir final Path tempDir) throws Exception {
        final MonthlyStatisticsReportGenerator generator =
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(tempDir));
        final MonthlyStatisticsSnapshot first = new MonthlyStatisticsSnapshot(
                1, 1, 0, 5, 5, 0, 0,
                List.of(new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                        LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 0)));
        final MonthlyStatisticsSnapshot second = new MonthlyStatisticsSnapshot(
                2, 2, 0, 8, 8, 0, 0,
                List.of(
                        new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                                LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 0),
                        new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                                LocalDateTime.of(2026, 6, 15, 10, 15), true, 3, 0)));

        generator.generate(new MonthlyReportRequest("AREA-0", YearMonth.of(2026, 6), first, LocalDateTime.now()));
        final MonthlyReportResult updated = generator.generate(new MonthlyReportRequest(
                "AREA-0", YearMonth.of(2026, 6), second, LocalDateTime.now()));

        assertTrue(updated.formattedContent().contains("Simulations run   : 2"));
    }

    @Test
    void generatorShowsNoSimulationsAsciiMessage(@TempDir final Path tempDir) {
        final MonthlyStatisticsReportGenerator generator =
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(tempDir));
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                0, 0, 0, 0, 0, 0, 0, List.of());

        final MonthlyReportResult result = generator.generate(new MonthlyReportRequest(
                "AREA-0", YearMonth.of(2026, 6), snapshot, LocalDateTime.now()));

        assertTrue(result.formattedContent().contains("No simulations in period."));
    }

    @Test
    void generatorRejectsNullRequest() {
        final MonthlyStatisticsReportGenerator generator =
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(Path.of("reports")));

        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void generatorRejectsNullPathResolver() {
        assertThrows(NullPointerException.class, () -> new MonthlyStatisticsReportGenerator(null));
    }

    @Test
    void generatorWrapsWriteFailures() throws Exception {
        final MonthlyReportsPathResolver resolver = mock(MonthlyReportsPathResolver.class);
        final Path target = Path.of("reports/monthly/AREA-0/2026-06-statistics.txt");
        doThrow(new IOException("disk full")).when(resolver).ensureParentDirectories(any());

        final MonthlyStatisticsReportGenerator generator = new MonthlyStatisticsReportGenerator(resolver);
        final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                1, 1, 0, 1, 1, 0, 0,
                List.of(new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                        LocalDateTime.of(2026, 6, 8, 14, 30), true, 1, 0)));

        final MonthlyReportRequest request = new MonthlyReportRequest(
                "AREA-0", YearMonth.of(2026, 6), snapshot, LocalDateTime.now());

        org.mockito.Mockito.when(resolver.resolveMonthlyFile("AREA-0", YearMonth.of(2026, 6)))
                .thenReturn(target);

        assertThrows(IllegalStateException.class, () -> generator.generate(request));
    }

    @Test
    void defaultConstructorIsUsable(@TempDir final Path tempDir) throws Exception {
        final String property = System.getProperty("aisafe.reports.dir");
        try {
            System.setProperty("aisafe.reports.dir", tempDir.toString());
            final MonthlyStatisticsReportGenerator generator = new MonthlyStatisticsReportGenerator();
            final MonthlyStatisticsSnapshot snapshot = new MonthlyStatisticsSnapshot(
                    1, 1, 0, 1, 1, 0, 0, List.of());
            final MonthlyReportResult result = generator.generate(new MonthlyReportRequest(
                    "AREA-0", YearMonth.of(2026, 6), snapshot, LocalDateTime.now()));
            assertTrue(Files.isRegularFile(result.reportPath()));
        } finally {
            if (property == null) {
                System.clearProperty("aisafe.reports.dir");
            } else {
                System.setProperty("aisafe.reports.dir", property);
            }
        }
    }
}
