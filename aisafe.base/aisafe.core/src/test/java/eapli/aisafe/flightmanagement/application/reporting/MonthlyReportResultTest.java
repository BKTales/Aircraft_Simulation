package eapli.aisafe.flightmanagement.application.reporting;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyReportResultTest {

    @Test
    void exposesPathAndContent() {
        final Path path = Path.of("reports/monthly/AREA-0/2026-06-statistics.txt");
        final MonthlyReportResult result = new MonthlyReportResult(path, "report body");

        assertEquals(path, result.reportPath());
        assertEquals("report body", result.formattedContent());
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class,
                () -> new MonthlyReportResult(null, "content"));
        assertThrows(NullPointerException.class,
                () -> new MonthlyReportResult(Path.of("x"), null));
    }
}
