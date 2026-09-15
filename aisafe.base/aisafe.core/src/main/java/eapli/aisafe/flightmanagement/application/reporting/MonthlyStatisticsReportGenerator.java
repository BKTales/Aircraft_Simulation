package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.infrastructure.reporting.MonthlyReportsPathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class MonthlyStatisticsReportGenerator
        implements ReportGenerator<MonthlyReportRequest, MonthlyReportResult> {

    private static final String LINE = "================================================================================";
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter BREAKDOWN_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ASCII_BAR_WIDTH = 20;

    private final MonthlyReportsPathResolver pathResolver;

    public MonthlyStatisticsReportGenerator() {
        this(new MonthlyReportsPathResolver());
    }

    public MonthlyStatisticsReportGenerator(final MonthlyReportsPathResolver pathResolver) {
        this.pathResolver = Objects.requireNonNull(pathResolver, "pathResolver");
    }

    @Override
    public MonthlyReportResult generate(final MonthlyReportRequest request) {
        Objects.requireNonNull(request, "request");
        final String content = buildContent(request);
        final Path reportPath = pathResolver.resolveMonthlyFile(request.areaCode(), request.period());
        try {
            pathResolver.ensureParentDirectories(reportPath);
            Files.writeString(reportPath, content);
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to write monthly statistics report.", ex);
        }
        return new MonthlyReportResult(reportPath.toAbsolutePath(), content);
    }

    String buildContent(final MonthlyReportRequest request) {
        final MonthlyStatisticsSnapshot snapshot = request.snapshot();
        final StringBuilder out = new StringBuilder();

        out.append(LINE).append('\n');
        out.append(" AISafe MONTHLY STATISTICS REPORT\n");
        out.append(LINE).append('\n');
        out.append(" Area              : ").append(request.areaCode()).append('\n');
        out.append(" Period            : ").append(request.period()).append('\n');
        out.append(" Generated at      : ")
                .append(GENERATED_FORMAT.format(request.generatedAt()))
                .append('\n');
        out.append('\n');
        out.append(" SIMULATION ACTIVITY\n");
        out.append("   Simulations run   : ").append(snapshot.simulationsRun()).append('\n');
        out.append("   Passed            : ").append(snapshot.passed()).append('\n');
        out.append("   Failed            : ").append(snapshot.failed()).append('\n');
        out.append("   Pass rate         : ")
                .append(String.format(Locale.US, "%.1f%%", snapshot.passRatePercent()))
                .append('\n');
        out.append('\n');
        out.append(" FLIGHT STATISTICS\n");
        out.append("   Total flights     : ").append(snapshot.totalFlights()).append('\n');
        out.append("   Successful exec.  : ").append(snapshot.successfulExecutions()).append('\n');
        out.append("   Failed execution  : ").append(snapshot.failedExecutions()).append('\n');
        out.append('\n');
        out.append(" SAFETY OVERVIEW\n");
        out.append("   Total violations  : ").append(snapshot.totalViolations()).append('\n');
        out.append('\n');
        out.append(" PER-SIMULATION BREAKDOWN\n");
        out.append("   Date/Time              Result  Flights  Violations\n");
        for (final MonthlyStatisticsSnapshot.SimulationBreakdownRow row : snapshot.breakdown()) {
            out.append("   ")
                    .append(BREAKDOWN_FORMAT.format(row.dateTime()))
                    .append("    ")
                    .append(row.passed() ? "PASS" : "FAIL")
                    .append("    ")
                    .append(row.flights())
                    .append("        ")
                    .append(row.violations())
                    .append('\n');
        }
        out.append('\n');
        appendAsciiChart(out, snapshot.passed(), snapshot.failed());
        out.append(LINE).append('\n');
        return out.toString();
    }

    private static void appendAsciiChart(final StringBuilder out, final int passed, final int failed) {
        out.append(" PASS/FAIL CHART (ASCII)\n");
        final int total = passed + failed;
        if (total == 0) {
            out.append("   No simulations in period.\n");
            return;
        }
        out.append("   PASS ").append(buildBar(passed, total)).append(' ').append(passed).append('\n');
        out.append("   FAIL ").append(buildBar(failed, total)).append(' ').append(failed).append('\n');
    }

    private static String buildBar(final int count, final int total) {
        final int filled = Math.min(ASCII_BAR_WIDTH, (int) Math.round((double) count / total * ASCII_BAR_WIDTH));
        return "[" + "#".repeat(filled) + "-".repeat(ASCII_BAR_WIDTH - filled) + "]";
    }
}
