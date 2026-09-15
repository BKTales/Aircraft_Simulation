package eapli.aisafe.flightmanagement.application.reporting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MonthlyStatisticsAggregator {

    public MonthlyStatisticsSnapshot aggregate(final List<SimulationSummaryRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("At least one simulation summary is required.");
        }

        int passed = 0;
        int failed = 0;
        int totalFlights = 0;
        int successfulExecutions = 0;
        int failedExecutions = 0;
        int totalViolations = 0;
        final List<MonthlyStatisticsSnapshot.SimulationBreakdownRow> breakdown = new ArrayList<>();

        for (final SimulationSummaryRecord record : records) {
            if (record.passed()) {
                passed++;
            } else {
                failed++;
            }
            totalFlights += record.totalFlights();
            successfulExecutions += record.successfulExecutions();
            failedExecutions += record.failedExecutions();
            totalViolations += record.violations();
            breakdown.add(new MonthlyStatisticsSnapshot.SimulationBreakdownRow(
                    record.generatedAt(),
                    record.passed(),
                    record.totalFlights(),
                    record.violations()));
        }

        breakdown.sort(Comparator.comparing(MonthlyStatisticsSnapshot.SimulationBreakdownRow::dateTime));

        return new MonthlyStatisticsSnapshot(
                records.size(),
                passed,
                failed,
                totalFlights,
                successfulExecutions,
                failedExecutions,
                totalViolations,
                breakdown);
    }
}
