package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyStatisticsCollectorTest {

    @Test
    void collectorIgnoresOtherMonths(@TempDir final Path tempDir) throws Exception {
        writeSummary(tempDir, "AREA-0", "2026-06-08_143000-summary.txt",
                Us112SummaryFixtures.passSummary("2026-06-08 14:30:00"));
        writeSummary(tempDir, "AREA-0", "2026-07-01_090000-summary.txt",
                Us112SummaryFixtures.passSummary("2026-07-01 09:00:00"));

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        final List<SimulationSummaryRecord> june = collector.collectSummaries("AREA-0", YearMonth.of(2026, 6));

        assertEquals(1, june.size());
        assertTrue(june.get(0).passed());
    }

    @Test
    void collectorIgnoresOtherAreas(@TempDir final Path tempDir) throws Exception {
        writeSummary(tempDir, "AREA-0", "2026-06-08_143000-summary.txt",
                Us112SummaryFixtures.passSummary("2026-06-08 14:30:00"));
        writeSummary(tempDir, "AREA-4", "2026-06-09_101500-summary.txt",
                Us112SummaryFixtures.passSummary("2026-06-09 10:15:00"));

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        final List<SimulationSummaryRecord> area0 = collector.collectSummaries("AREA-0", YearMonth.of(2026, 6));

        assertEquals(1, area0.size());
    }

    @Test
    void returnsEmptyWhenAreaDirectoryMissing(@TempDir final Path tempDir) {
        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        assertTrue(collector.collectSummaries("AREA-9", YearMonth.of(2026, 6)).isEmpty());
    }

    @Test
    void rejectsInvalidArguments() {
        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(Path.of("reports")), new SimulationSummaryFileParser());

        assertThrows(IllegalArgumentException.class, () -> collector.collectSummaries(null, YearMonth.of(2026, 6)));
        assertThrows(IllegalArgumentException.class, () -> collector.collectSummaries("  ", YearMonth.of(2026, 6)));
        assertThrows(IllegalArgumentException.class, () -> collector.collectSummaries("AREA-0", null));
    }

    @Test
    void defaultConstructorIsUsable() {
        assertNotNull(new SimulationSummaryArchiveCollector());
    }

    @Test
    void rejectsNullDependencies() {
        final SimulationReportsPathResolver resolver = new SimulationReportsPathResolver(Path.of("reports"));
        final SimulationSummaryFileParser parser = new SimulationSummaryFileParser();

        assertThrows(NullPointerException.class, () -> new SimulationSummaryArchiveCollector(null, parser));
        assertThrows(NullPointerException.class, () -> new SimulationSummaryArchiveCollector(resolver, null));
    }

    @Test
    void ignoresShortFilenames(@TempDir final Path tempDir) throws Exception {
        final Path dir = tempDir.resolve("simulations").resolve("AREA-0");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("x.txt"), Us112SummaryFixtures.passSummary("2026-06-08 14:30:00"));

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        assertTrue(collector.collectSummaries("AREA-0", YearMonth.of(2026, 6)).isEmpty());
    }

    @Test
    void returnsEmptyWhenAreaPathIsFile(@TempDir final Path tempDir) throws Exception {
        final Path areaPath = tempDir.resolve("simulations").resolve("AREA-0");
        Files.createDirectories(areaPath.getParent());
        Files.writeString(areaPath, "not-a-directory");

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        assertTrue(collector.collectSummaries("AREA-0", YearMonth.of(2026, 6)).isEmpty());
    }

    @Test
    void failsWhenSummaryPathIsDirectory(@TempDir final Path tempDir) throws Exception {
        final Path dir = tempDir.resolve("simulations").resolve("AREA-0");
        Files.createDirectories(dir);
        Files.createDirectories(dir.resolve("2026-06-08_143000-summary.txt"));

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        assertThrows(IllegalStateException.class,
                () -> collector.collectSummaries("AREA-0", YearMonth.of(2026, 6)));
    }

    @Test
    void failsWhenGeneratedAtMonthDiffersFromFilename(@TempDir final Path tempDir) throws Exception {
        writeSummary(tempDir, "AREA-0", "2026-06-08_143000-summary.txt",
                Us112SummaryFixtures.passSummary("2026-07-01 09:00:00"));

        final SimulationSummaryArchiveCollector collector = new SimulationSummaryArchiveCollector(
                new SimulationReportsPathResolver(tempDir), new SimulationSummaryFileParser());

        assertThrows(IllegalStateException.class,
                () -> collector.collectSummaries("AREA-0", YearMonth.of(2026, 6)));
    }

    private static void writeSummary(final Path baseDir,
                                     final String areaCode,
                                     final String fileName,
                                     final String content) throws Exception {
        final Path dir = baseDir.resolve("simulations").resolve(areaCode);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), content);
    }
}
