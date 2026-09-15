package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.SafetyViolationEvent;
import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportDesignatorRewriter;
import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorFlightDesignatorMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public final class SimulationSummaryReportGenerator
        implements ReportGenerator<SimulationSummaryRequest, SimulationSummaryResult> {

    private static final String LINE = "================================================================================";
    private static final DateTimeFormatter INTERVAL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SimulationReportsPathResolver pathResolver;

    public SimulationSummaryReportGenerator() {
        this(new SimulationReportsPathResolver());
    }

    public SimulationSummaryReportGenerator(final SimulationReportsPathResolver pathResolver) {
        this.pathResolver = Objects.requireNonNull(pathResolver, "pathResolver");
    }

    @Override
    public SimulationSummaryResult generate(final SimulationSummaryRequest request) {
        Objects.requireNonNull(request, "request");
        final String content = buildContent(request);
        final Path summaryPath = pathResolver.resolveSummaryFile(request.areaCode(), request.generatedAt());
        try {
            pathResolver.ensureParentDirectories(summaryPath);
            Files.writeString(summaryPath, content);
            archiveSourceReports(request);
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to write simulation summary report.", ex);
        }
        return new SimulationSummaryResult(summaryPath.toAbsolutePath(), content);
    }

    String buildContent(final SimulationSummaryRequest request) {
        final FlightSimulationReport report = request.report();
        final int totalFlights = report.flightStatuses().size();
        final long successCount = report.flightStatuses().stream()
                .filter(status -> status.executionStatus() == FlightExecutionStatus.SUCCESS)
                .count();
        final int failedCount = totalFlights - (int) successCount;
        final StringBuilder out = new StringBuilder();

        out.append(LINE).append('\n');
        out.append(" AISafe SIMULATION SUMMARY REPORT\n");
        out.append(LINE).append('\n');
        out.append(" Area              : ").append(request.areaCode()).append('\n');
        out.append(" Interval          : ")
                .append(INTERVAL_FORMAT.format(request.intervalStart()))
                .append(" -> ")
                .append(INTERVAL_FORMAT.format(request.intervalEnd()))
                .append('\n');
        out.append(" Generated at      : ")
                .append(GENERATED_FORMAT.format(request.generatedAt()))
                .append('\n');
        out.append('\n');
        out.append(" FINAL RESULT      : ")
                .append(report.passed() ? "PASS" : "FAIL")
                .append('\n');
        out.append(" Total flights     : ").append(totalFlights).append('\n');
        out.append(" Completed (SUCCESS): ").append(successCount).append('\n');
        out.append(" Failed execution  : ").append(failedCount).append('\n');
        out.append('\n');
        out.append(" FLIGHT EXECUTION STATUS\n");
        final Map<String, String> designators = request.designatorBySimulatorId();
        for (final FlightStatus status : report.flightStatuses()) {
            out.append("   ")
                    .append(SimulatorFlightDesignatorMap.resolve(designators, status.flightId()))
                    .append("  ")
                    .append(status.executionStatus())
                    .append('\n');
        }
        out.append('\n');
        appendViolations(out, report, designators);
        out.append(LINE).append('\n');
        return out.toString();
    }

    private static void appendViolations(final StringBuilder out,
                                         final FlightSimulationReport report,
                                         final Map<String, String> designators) {
        final int count = report.safetyViolations().size();
        out.append(" SAFETY VIOLATIONS (").append(count).append(")\n");
        if (count == 0) {
            out.append(" No safety violations recorded.\n");
            return;
        }
        int index = 1;
        for (final SafetyViolationEvent event : report.safetyViolations()) {
            out.append("   [").append(index++).append("] ")
                    .append(event.violationType())
                    .append(" — ")
                    .append(event.timestampLabel())
                    .append('\n');
            out.append("       Flight A (")
                    .append(SimulatorFlightDesignatorMap.resolve(designators, event.flightIdA()))
                    .append("): ")
                    .append(event.positionA())
                    .append('\n');
            out.append("       Flight B (")
                    .append(SimulatorFlightDesignatorMap.resolve(designators, event.flightIdB()))
                    .append("): ")
                    .append(event.positionB())
                    .append('\n');
        }
    }

    private void archiveSourceReports(final SimulationSummaryRequest request) throws IOException {
        final Map<String, String> designators = request.designatorBySimulatorId();
        if (request.sourceCsv() != null && Files.isRegularFile(request.sourceCsv())) {
            final Path target = pathResolver.resolveArchiveCsv(request.areaCode(), request.generatedAt());
            pathResolver.ensureParentDirectories(target);
            final String csv = Files.readString(request.sourceCsv());
            Files.writeString(target, SimulationReportDesignatorRewriter.rewriteCsv(csv, designators));
        }
        if (request.sourceTxt() != null && Files.isRegularFile(request.sourceTxt())) {
            final Path target = pathResolver.resolveArchiveTxt(request.areaCode(), request.generatedAt());
            pathResolver.ensureParentDirectories(target);
            final String txt = Files.readString(request.sourceTxt());
            Files.writeString(target, SimulationReportDesignatorRewriter.rewriteTxt(txt, designators));
        }
    }
}
