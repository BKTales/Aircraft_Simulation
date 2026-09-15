package eapli.aisafe.flightmanagement.application.reporting;

import java.time.LocalDateTime;
import java.util.Objects;

public final class SimulationSummaryRecord {

    private final LocalDateTime generatedAt;
    private final boolean passed;
    private final int totalFlights;
    private final int successfulExecutions;
    private final int failedExecutions;
    private final int violations;

    public SimulationSummaryRecord(final LocalDateTime generatedAt,
                                   final boolean passed,
                                   final int totalFlights,
                                   final int successfulExecutions,
                                   final int failedExecutions,
                                   final int violations) {
        this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt");
        this.passed = passed;
        this.totalFlights = totalFlights;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.violations = violations;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public boolean passed() {
        return passed;
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

    public int violations() {
        return violations;
    }
}
