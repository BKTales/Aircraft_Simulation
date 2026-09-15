package eapli.aisafe.flightmanagement.application.reporting;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

public final class MonthlyReportRequest {

    private final String areaCode;
    private final YearMonth period;
    private final MonthlyStatisticsSnapshot snapshot;
    private final LocalDateTime generatedAt;

    public MonthlyReportRequest(final String areaCode,
                                final YearMonth period,
                                final MonthlyStatisticsSnapshot snapshot,
                                final LocalDateTime generatedAt) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        this.areaCode = areaCode.trim();
        this.period = Objects.requireNonNull(period, "period");
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
    }

    public String areaCode() {
        return areaCode;
    }

    public YearMonth period() {
        return period;
    }

    public MonthlyStatisticsSnapshot snapshot() {
        return snapshot;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }
}
