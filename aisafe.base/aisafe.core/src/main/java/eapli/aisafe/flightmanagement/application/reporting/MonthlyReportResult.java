package eapli.aisafe.flightmanagement.application.reporting;

import java.nio.file.Path;
import java.util.Objects;

public final class MonthlyReportResult {

    private final Path reportPath;
    private final String formattedContent;

    public MonthlyReportResult(final Path reportPath, final String formattedContent) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
        this.formattedContent = Objects.requireNonNull(formattedContent, "formattedContent");
    }

    public Path reportPath() {
        return reportPath;
    }

    public String formattedContent() {
        return formattedContent;
    }
}
