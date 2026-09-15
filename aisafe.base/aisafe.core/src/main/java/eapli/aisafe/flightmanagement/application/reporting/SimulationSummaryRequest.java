package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public final class SimulationSummaryRequest {

    private final String areaCode;
    private final LocalDateTime intervalStart;
    private final LocalDateTime intervalEnd;
    private final FlightSimulationReport report;
    private final Path sourceCsv;
    private final Path sourceTxt;
    private final LocalDateTime generatedAt;
    private final Map<String, String> designatorBySimulatorId;

    public SimulationSummaryRequest(final String areaCode,
                                    final LocalDateTime intervalStart,
                                    final LocalDateTime intervalEnd,
                                    final FlightSimulationReport report,
                                    final Path sourceCsv,
                                    final Path sourceTxt,
                                    final LocalDateTime generatedAt) {
        this(areaCode, intervalStart, intervalEnd, report, sourceCsv, sourceTxt, generatedAt, Map.of());
    }

    public SimulationSummaryRequest(final String areaCode,
                                    final LocalDateTime intervalStart,
                                    final LocalDateTime intervalEnd,
                                    final FlightSimulationReport report,
                                    final Path sourceCsv,
                                    final Path sourceTxt,
                                    final LocalDateTime generatedAt,
                                    final Map<String, String> designatorBySimulatorId) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (intervalStart == null || intervalEnd == null) {
            throw new IllegalArgumentException("Simulation interval is required.");
        }
        this.areaCode = areaCode.trim();
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
        this.report = Objects.requireNonNull(report, "report");
        this.sourceCsv = sourceCsv;
        this.sourceTxt = sourceTxt;
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.designatorBySimulatorId = designatorBySimulatorId != null
                ? Map.copyOf(designatorBySimulatorId)
                : Map.of();
    }

    public String areaCode() {
        return areaCode;
    }

    public LocalDateTime intervalStart() {
        return intervalStart;
    }

    public LocalDateTime intervalEnd() {
        return intervalEnd;
    }

    public FlightSimulationReport report() {
        return report;
    }

    public Path sourceCsv() {
        return sourceCsv;
    }

    public Path sourceTxt() {
        return sourceTxt;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public Map<String, String> designatorBySimulatorId() {
        return designatorBySimulatorId;
    }
}
