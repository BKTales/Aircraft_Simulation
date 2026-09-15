package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.application.exceptions.NoMonthlySimulationDataException;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportRequest;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportResult;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsAggregator;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsReportGenerator;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyStatisticsSnapshot;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryArchiveCollector;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryRecord;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class GenerateMonthlyStatisticsReportService {

    private final SimulationSummaryArchiveCollector collector;
    private final MonthlyStatisticsAggregator aggregator;
    private final MonthlyStatisticsReportGenerator generator;

    public GenerateMonthlyStatisticsReportService() {
        this(new SimulationSummaryArchiveCollector(),
                new MonthlyStatisticsAggregator(),
                new MonthlyStatisticsReportGenerator());
    }

    public GenerateMonthlyStatisticsReportService(final SimulationSummaryArchiveCollector collector,
                                                  final MonthlyStatisticsAggregator aggregator,
                                                  final MonthlyStatisticsReportGenerator generator) {
        this.collector = Objects.requireNonNull(collector, "collector");
        this.aggregator = Objects.requireNonNull(aggregator, "aggregator");
        this.generator = Objects.requireNonNull(generator, "generator");
    }

    public MonthlyReportResult generateMonthlyReport(final String areaCode, final YearMonth period) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (period == null) {
            throw new IllegalArgumentException("Period is required.");
        }

        final List<SimulationSummaryRecord> records = collector.collectSummaries(areaCode, period);
        if (records.isEmpty()) {
            throw new NoMonthlySimulationDataException(areaCode, period);
        }

        final MonthlyStatisticsSnapshot snapshot = aggregator.aggregate(records);
        return generator.generate(new MonthlyReportRequest(
                areaCode,
                period,
                snapshot,
                LocalDateTime.now()));
    }
}
