package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class SimulationSummaryArchiveCollector {

    private final SimulationReportsPathResolver simulationPathResolver;
    private final SimulationSummaryFileParser parser;

    public SimulationSummaryArchiveCollector() {
        this(new SimulationReportsPathResolver(), new SimulationSummaryFileParser());
    }

    public SimulationSummaryArchiveCollector(final SimulationReportsPathResolver simulationPathResolver,
                                             final SimulationSummaryFileParser parser) {
        this.simulationPathResolver = Objects.requireNonNull(simulationPathResolver, "simulationPathResolver");
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public List<SimulationSummaryRecord> collectSummaries(final String areaCode, final YearMonth period) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (period == null) {
            throw new IllegalArgumentException("Period is required.");
        }

        final Path areaDir = simulationPathResolver.baseDir()
                .resolve("simulations")
                .resolve(areaCode.trim());
        if (!Files.isDirectory(areaDir)) {
            return List.of();
        }

        final List<SimulationSummaryRecord> records = new ArrayList<>();
        try (Stream<Path> files = Files.list(areaDir)) {
            files.filter(path -> path.getFileName().toString().endsWith("-summary.txt"))
                    .filter(path -> filenameMatchesPeriod(path, period))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> records.add(readAndParse(path, period)));
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to list simulation summaries for " + areaCode, ex);
        }
        return records;
    }

    private SimulationSummaryRecord readAndParse(final Path path, final YearMonth period) {
        try {
            final SimulationSummaryRecord record = parser.parse(Files.readString(path));
            if (!YearMonth.from(record.generatedAt()).equals(period)) {
                throw new IllegalStateException("Summary " + path.getFileName() + " is outside period " + period);
            }
            return record;
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to read summary file: " + path, ex);
        }
    }

    private static boolean filenameMatchesPeriod(final Path path, final YearMonth period) {
        final String name = path.getFileName().toString();
        if (name.length() < 7) {
            return false;
        }
        final String prefix = name.substring(0, 7);
        return prefix.equals(String.format("%04d-%02d", period.getYear(), period.getMonthValue()));
    }
}
