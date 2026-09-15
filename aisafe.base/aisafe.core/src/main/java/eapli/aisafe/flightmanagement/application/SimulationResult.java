package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SimulationResult {

    private final FlightSimulationReport report;
    private final int exportedPlans;
    private final String reportCsvPath;
    private final String flightPlansDirectory;
    private final String summaryReportPath;
    private final String summaryContent;
    private final List<String> simulationLog;

    public SimulationResult(final FlightSimulationReport report,
                            final int exportedPlans,
                            final String reportCsvPath,
                            final String flightPlansDirectory) {
        this(report, exportedPlans, reportCsvPath, flightPlansDirectory, null, null, List.of());
    }

    public SimulationResult(final FlightSimulationReport report,
                            final int exportedPlans,
                            final String reportCsvPath,
                            final String flightPlansDirectory,
                            final String summaryReportPath,
                            final String summaryContent) {
        this(report, exportedPlans, reportCsvPath, flightPlansDirectory, summaryReportPath, summaryContent, List.of());
    }

    public SimulationResult(final FlightSimulationReport report,
                            final int exportedPlans,
                            final String reportCsvPath,
                            final String flightPlansDirectory,
                            final String summaryReportPath,
                            final String summaryContent,
                            final List<String> simulationLog) {
        this.report = Objects.requireNonNull(report, "report");
        if (exportedPlans < 0) {
            throw new IllegalArgumentException("exportedPlans cannot be negative.");
        }
        this.exportedPlans = exportedPlans;
        this.reportCsvPath = Objects.requireNonNull(reportCsvPath, "reportCsvPath");
        this.flightPlansDirectory = Objects.requireNonNull(flightPlansDirectory, "flightPlansDirectory");
        this.summaryReportPath = summaryReportPath;
        this.summaryContent = summaryContent;
        this.simulationLog = simulationLog == null ? List.of() : List.copyOf(simulationLog);
    }

    public FlightSimulationReport report() {
        return report;
    }

    public boolean passed() {
        return report.passed();
    }

    public int exportedPlans() {
        return exportedPlans;
    }

    public String csvPath() {
        return reportCsvPath;
    }

    public String reportDirectory() {
        final Path parent = Path.of(reportCsvPath).getParent();
        return parent != null ? parent.toAbsolutePath().toString() : reportCsvPath;
    }

    public String flightPlansDirectory() {
        return flightPlansDirectory;
    }

    public Optional<String> summaryReportPath() {
        return Optional.ofNullable(summaryReportPath);
    }

    public Optional<String> summaryContent() {
        return Optional.ofNullable(summaryContent);
    }

    public boolean hasSummary() {
        return summaryReportPath != null;
    }

    public List<String> simulationLog() {
        return simulationLog;
    }

    public String failureReason() {
        if (passed()) {
            return "";
        }
        final Set<String> reasons = new LinkedHashSet<>();
        for (final FlightStatus status : report.flightStatuses()) {
            if (status.executionStatus() != FlightExecutionStatus.SUCCESS) {
                reasons.add(status.executionStatus().userMessage());
            }
        }
        if (!reasons.isEmpty()) {
            return String.join("; ", reasons);
        }
        return "Validation result: " + report.validationResult();
    }
}
