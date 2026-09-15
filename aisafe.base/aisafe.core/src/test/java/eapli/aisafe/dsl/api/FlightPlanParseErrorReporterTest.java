package eapli.aisafe.dsl.api;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanParseErrorReporterTest {

    @Test
    void printsSourceLineAndCaretForLocatedError() {
        final String source = "flight X {\n  type REGULAR\n}\n";
        final List<String> errors = List.of("Line 2:2 - Token inesperado 'REGULAR'.");

        final String report = capture(source, errors);

        assertTrue(report.contains("INVALID FLIGHT PLAN"));
        assertTrue(report.contains("line 2, column 2"));
        assertTrue(report.contains("type REGULAR"));
        assertTrue(report.contains("^ here"));
        assertTrue(report.contains("Token inesperado"));
    }

    @Test
    void printsOtherErrorsWithoutLocation() {
        final String report = capture("content", List.of("Unsupported file format '.json'."));

        assertTrue(report.contains("Other issues"));
        assertTrue(report.contains("Unsupported file format"));
        assertFalse(report.contains("^ here"));
    }

    @Test
    void mixesLocatedAndOtherErrors() {
        final String source = "a\nb\nc\n";
        final List<String> errors = List.of(
                "Line 2:0 - syntax problem.",
                "Generic failure.");

        final String report = capture(source, errors);

        assertTrue(report.contains("Error 1 of 1"));
        assertTrue(report.contains("Generic failure"));
    }

    private static String capture(final String source, final List<String> errors) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FlightPlanParseErrorReporter.print(new PrintStream(buffer, true, StandardCharsets.UTF_8), source, errors);
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
