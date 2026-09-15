package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.application.exceptions.NoMonthlySimulationDataException;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportResult;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsReportGenerator;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryArchiveCollector;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryFileParser;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryRecord;
import eapli.aisafe.flightmanagement.application.reporting.Us112SummaryFixtures;
import eapli.aisafe.flightmanagement.infrastructure.reporting.MonthlyReportsPathResolver;
import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMonthlyStatisticsReportServiceTest {

    @Test
    void serviceFailsWhenNoSummaries(@TempDir final Path tempDir) {
        final GenerateMonthlyStatisticsReportService service = new GenerateMonthlyStatisticsReportService(
                new SimulationSummaryArchiveCollector(
                        new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser()),
                new eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator(),
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(tempDir)));

        assertThrows(NoMonthlySimulationDataException.class,
                () -> service.generateMonthlyReport("AREA-0", YearMonth.of(2026, 6)));
    }

    @Test
    void serviceGeneratesReportFromFixtures(@TempDir final Path tempDir) throws Exception {
        final Path areaDir = tempDir.resolve("simulations/AREA-0");
        Files.createDirectories(areaDir);
        Files.writeString(areaDir.resolve("2026-06-08_143000-summary.txt"),
                Us112SummaryFixtures.passSummary("2026-06-08 14:30:00"));
        Files.writeString(areaDir.resolve("2026-06-10_091500-summary.txt"),
                Us112SummaryFixtures.failSummary("2026-06-10 09:15:00"));

        final GenerateMonthlyStatisticsReportService service = new GenerateMonthlyStatisticsReportService(
                new SimulationSummaryArchiveCollector(
                        new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser()),
                new eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator(),
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(tempDir)));

        final MonthlyReportResult result = service.generateMonthlyReport("AREA-0", YearMonth.of(2026, 6));

        assertTrue(Files.isRegularFile(result.reportPath()));
        assertTrue(result.formattedContent().contains("Simulations run   : 2"));
    }

    @Test
    void rejectsInvalidArguments() {
        final GenerateMonthlyStatisticsReportService service = new GenerateMonthlyStatisticsReportService(
                new SimulationSummaryArchiveCollector(
                        new SimulationReportsPathResolver(Path.of("reports")), new SimulationSummaryFileParser()),
                new eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator(),
                new MonthlyStatisticsReportGenerator(new MonthlyReportsPathResolver(Path.of("reports"))));

        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(null, YearMonth.of(2026, 6)));
        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport("  ", YearMonth.of(2026, 6)));
        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport("AREA-0", null));
    }

    @Test
    void rejectsNullDependencies() {
        final SimulationSummaryArchiveCollector collector = mock(SimulationSummaryArchiveCollector.class);
        final eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator aggregator =
                new eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator();
        final MonthlyStatisticsReportGenerator generator = mock(MonthlyStatisticsReportGenerator.class);

        assertThrows(NullPointerException.class,
                () -> new GenerateMonthlyStatisticsReportService(null, aggregator, generator));
        assertThrows(NullPointerException.class,
                () -> new GenerateMonthlyStatisticsReportService(collector, null, generator));
        assertThrows(NullPointerException.class,
                () -> new GenerateMonthlyStatisticsReportService(collector, aggregator, null));
    }

    @Test
    void defaultConstructorIsUsable() {
        assertNotNull(new GenerateMonthlyStatisticsReportService());
    }

    @Test
    void delegatesToCollectorAggregatorAndGenerator() {
        final SimulationSummaryArchiveCollector collector = mock(SimulationSummaryArchiveCollector.class);
        final eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator aggregator =
                new eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator();
        final MonthlyStatisticsReportGenerator generator = mock(MonthlyStatisticsReportGenerator.class);
        final GenerateMonthlyStatisticsReportService service =
                new GenerateMonthlyStatisticsReportService(collector, aggregator, generator);

        final SimulationSummaryRecord record = new SimulationSummaryRecord(
                java.time.LocalDateTime.of(2026, 6, 8, 14, 30), true, 5, 5, 0, 0);
        when(collector.collectSummaries(eq("AREA-0"), eq(YearMonth.of(2026, 6))))
                .thenReturn(java.util.List.of(record));
        final MonthlyReportResult expected = new MonthlyReportResult(
                Path.of("reports/monthly/AREA-0/2026-06-statistics.txt"), "ok");
        when(generator.generate(any())).thenReturn(expected);

        final MonthlyReportResult result = service.generateMonthlyReport("AREA-0", YearMonth.of(2026, 6));

        assertEquals(expected, result);
        verify(collector).collectSummaries("AREA-0", YearMonth.of(2026, 6));
        verify(generator).generate(any());
    }
}
