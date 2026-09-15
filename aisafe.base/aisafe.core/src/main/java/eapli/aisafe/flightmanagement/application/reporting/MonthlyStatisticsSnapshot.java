package eapli.aisafe.flightmanagement.application.reporting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class MonthlyStatisticsSnapshot {

    public record SimulationBreakdownRow(
            LocalDateTime dateTime,
            boolean passed,
            int flights,
            int violations) {
    }

    private final int simulationsRun;
    private final int passed;
    private final int failed;
    private final int totalFlights;
    private final int successfulExecutions;
    private final int failedExecutions;
    private final int totalViolations;
    private final List<SimulationBreakdownRow> breakdown;

    public MonthlyStatisticsSnapshot(final int simulationsRun,
                                     final int passed,
                                     final int failed,
                                     final int totalFlights,
                                     final int successfulExecutions,
                                     final int failedExecutions,
                                     final int totalViolations,
                                     final List<SimulationBreakdownRow> breakdown) {
        this.simulationsRun = simulationsRun;
        this.passed = passed;
        this.failed = failed;
        this.totalFlights = totalFlights;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.totalViolations = totalViolations;
        this.breakdown = List.copyOf(Objects.requireNonNull(breakdown, "breakdown"));
    }

    public int simulationsRun() {
        return simulationsRun;
    }

    public int passed() {
        return passed;
    }

    public int failed() {
        return failed;
    }

    public double passRatePercent() {
        if (simulationsRun == 0) {
            return 0.0;
        }
        return (100.0 * passed) / simulationsRun;
    }

    public int totalFlights() {
        return totalFlights;
    }

    public int successfulExecutions() {
        return successfulExecutions;
    }

    public int failedExecutions() {
        return failedExecutions;
    }

    public int totalViolations() {
        return totalViolations;
    }

    public List<SimulationBreakdownRow> breakdown() {
        return breakdown;
    }
}
