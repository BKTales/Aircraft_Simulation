package eapli.aisafe.flightmanagement.infrastructure.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class SimulationReportsPathResolver {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private final Path baseDir;

    public SimulationReportsPathResolver() {
        this(resolveBaseDir());
    }

    public SimulationReportsPathResolver(final Path baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory is required.");
        }
        this.baseDir = baseDir;
    }

    public Path baseDir() {
        return baseDir;
    }

    public Path resolveSummaryFile(final String areaCode, final LocalDateTime generatedAt) {
        validateAreaCode(areaCode);
        return areaDirectory(areaCode).resolve(timestampPrefix(generatedAt) + "-summary.txt");
    }

    public Path resolveArchiveCsv(final String areaCode, final LocalDateTime generatedAt) {
        validateAreaCode(areaCode);
        return areaDirectory(areaCode).resolve(timestampPrefix(generatedAt) + "-report.csv");
    }

    public Path resolveArchiveTxt(final String areaCode, final LocalDateTime generatedAt) {
        validateAreaCode(areaCode);
        return areaDirectory(areaCode).resolve(timestampPrefix(generatedAt) + "-report.txt");
    }

    public void ensureParentDirectories(final Path targetFile) throws IOException {
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }
    }

    private Path areaDirectory(final String areaCode) {
        return baseDir.resolve("simulations").resolve(areaCode.trim());
    }

    private static String timestampPrefix(final LocalDateTime generatedAt) {
        return FILE_TIMESTAMP.format(generatedAt);
    }

    private static void validateAreaCode(final String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
    }

    private static Path resolveBaseDir() {
        final String env = System.getenv("AISAFE_REPORTS_DIR");
        if (env != null && !env.isBlank()) {
            return Path.of(env.trim());
        }
        final String property = System.getProperty("aisafe.reports.dir");
        if (property != null && !property.isBlank()) {
            return Path.of(property.trim());
        }
        return Path.of("reports");
    }
}
