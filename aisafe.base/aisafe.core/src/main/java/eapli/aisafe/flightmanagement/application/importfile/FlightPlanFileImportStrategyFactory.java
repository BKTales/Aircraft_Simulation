package eapli.aisafe.flightmanagement.application.importfile;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class FlightPlanFileImportStrategyFactory {

    private final List<FlightPlanFileImportStrategy> strategies;

    public FlightPlanFileImportStrategyFactory() {
        this(List.of(
                new TxtFlightPlanFileImportStrategy(),
                new DslFlightPlanFileImportStrategy(),
                new ExtensionlessFlightPlanFileImportStrategy()));
    }

    FlightPlanFileImportStrategyFactory(final List<FlightPlanFileImportStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one import strategy is required.");
        }
        this.strategies = List.copyOf(strategies);
    }

    public FlightPlanFileImportResult parse(final Path path) {
        Objects.requireNonNull(path, "path");
        final String extension = FlightPlanFileExtensions.normalizeExtension(path);
        for (final FlightPlanFileImportStrategy strategy : strategies) {
            if (strategy.supports(extension)) {
                return strategy.parse(path);
            }
        }
        return unsupportedFormat(extension);
    }

    public boolean isSupportedPath(final Path path) {
        return strategies.stream()
                .anyMatch(s -> s.supports(FlightPlanFileExtensions.normalizeExtension(path)));
    }

    private static FlightPlanFileImportResult unsupportedFormat(final String extension) {
        final String label = extension.isEmpty() ? "(none)" : "." + extension;
        return FlightPlanFileImportResult.failure(
                "Unsupported file format '" + label
                        + "'. Supported: .txt, .dsl, or no extension (Core Flight DSL).");
    }
}
