package eapli.aisafe.flightmanagement.infrastructure.reporting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationReportsPathResolverTest {

    @Test
    void ensureSummaryPathUsesAreaAndTimestamp(@TempDir final Path tempDir) {
        final SimulationReportsPathResolver resolver = new SimulationReportsPathResolver(tempDir);
        final LocalDateTime generatedAt = LocalDateTime.of(2026, 6, 8, 14, 30, 0);

        final Path summary = resolver.resolveSummaryFile("AREA-0", generatedAt);

        assertEquals(tempDir.resolve("simulations/AREA-0/2026-06-08_143000-summary.txt"), summary);
    }

    @Test
    void ensureParentDirectoriesAreCreated(@TempDir final Path tempDir) throws Exception {
        final SimulationReportsPathResolver resolver = new SimulationReportsPathResolver(tempDir);
        final Path summary = resolver.resolveSummaryFile("AREA-1", LocalDateTime.of(2026, 6, 8, 9, 0));

        resolver.ensureParentDirectories(summary);

        assertTrue(Files.isDirectory(tempDir.resolve("simulations/AREA-1")));
    }
}
