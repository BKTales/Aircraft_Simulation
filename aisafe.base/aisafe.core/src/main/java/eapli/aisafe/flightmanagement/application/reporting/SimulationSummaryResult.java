package eapli.aisafe.flightmanagement.application.reporting;

import java.nio.file.Path;
import java.util.Objects;

public final class SimulationSummaryResult {

    private final Path summaryPath;
    private final String formattedContent;

    public SimulationSummaryResult(final Path summaryPath, final String formattedContent) {
        this.summaryPath = Objects.requireNonNull(summaryPath, "summaryPath");
        this.formattedContent = Objects.requireNonNull(formattedContent, "formattedContent");
    }

    public Path summaryPath() {
        return summaryPath;
    }

    public String formattedContent() {
        return formattedContent;
    }
}
