package eapli.aisafe.flightmanagement.infrastructure.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

public final class MonthlyReportsPathResolver {

    private final Path baseDir;

    public MonthlyReportsPathResolver() {
        this(resolveBaseDir());
    }

    public MonthlyReportsPathResolver(final Path baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory is required.");
        }
        this.baseDir = baseDir;
    }

    public Path baseDir() {
        return baseDir;
    }

    public Path resolveMonthlyFile(final String areaCode, final YearMonth period) {
        validateAreaCode(areaCode);
        if (period == null) {
            throw new IllegalArgumentException("Period is required.");
        }
        final String fileName = String.format("%04d-%02d-statistics.txt", period.getYear(), period.getMonthValue());
        return baseDir.resolve("monthly").resolve(areaCode.trim()).resolve(fileName);
    }

    public void ensureParentDirectories(final Path targetFile) throws IOException {
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }
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
