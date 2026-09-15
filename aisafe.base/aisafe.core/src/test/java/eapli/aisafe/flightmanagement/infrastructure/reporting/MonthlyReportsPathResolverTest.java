package eapli.aisafe.flightmanagement.infrastructure.reporting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyReportsPathResolverTest {

    @Test
    void resolveMonthlyFileUsesAreaAndPeriod(@TempDir final Path tempDir) {
        final MonthlyReportsPathResolver resolver = new MonthlyReportsPathResolver(tempDir);

        final Path monthly = resolver.resolveMonthlyFile("AREA-0", YearMonth.of(2026, 6));

        assertEquals(tempDir.resolve("monthly/AREA-0/2026-06-statistics.txt"), monthly);
    }

    @Test
    void ensureParentDirectoriesAreCreated(@TempDir final Path tempDir) throws Exception {
        final MonthlyReportsPathResolver resolver = new MonthlyReportsPathResolver(tempDir);
        final Path monthly = resolver.resolveMonthlyFile("AREA-1", YearMonth.of(2026, 6));

        resolver.ensureParentDirectories(monthly);

        assertTrue(Files.isDirectory(tempDir.resolve("monthly/AREA-1")));
    }
}
