package eapli.aisafe.flightmanagement.application.reporting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimulationSummaryFileParser {

    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern VIOLATIONS_HEADER = Pattern.compile("SAFETY VIOLATIONS \\((\\d+)\\)");

    public SimulationSummaryRecord parse(final String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Summary content is required.");
        }

        LocalDateTime generatedAt = null;
        Boolean passed = null;
        int totalFlights = 0;
        int successfulExecutions = 0;
        int failedExecutions = 0;
        int violations = 0;

        for (final String rawLine : content.split("\n")) {
            final String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("Generated at")) {
                generatedAt = parseDateTime(valueAfterColon(line));
                continue;
            }
            if (line.startsWith("FINAL RESULT")) {
                passed = "PASS".equalsIgnoreCase(valueAfterColon(line));
                continue;
            }
            if (line.startsWith("Total flights")) {
                totalFlights = parseInt(valueAfterColon(line));
                continue;
            }
            if (line.startsWith("Completed (SUCCESS)")) {
                successfulExecutions = parseInt(valueAfterColon(line));
                continue;
            }
            if (line.startsWith("Failed execution")) {
                failedExecutions = parseInt(valueAfterColon(line));
                continue;
            }
            if (line.contains("SAFETY VIOLATIONS")) {
                final Matcher matcher = VIOLATIONS_HEADER.matcher(line);
                if (matcher.find()) {
                    violations = parseInt(matcher.group(1));
                }
            }
        }

        if (generatedAt == null) {
            throw new IllegalArgumentException("Summary is missing Generated at timestamp.");
        }
        if (passed == null) {
            throw new IllegalArgumentException("Summary is missing FINAL RESULT.");
        }

        return new SimulationSummaryRecord(
                generatedAt,
                passed,
                totalFlights,
                successfulExecutions,
                failedExecutions,
                violations);
    }

    private static String valueAfterColon(final String line) {
        final int colon = line.indexOf(':');
        if (colon < 0) {
            return "";
        }
        return line.substring(colon + 1).trim();
    }

    private static int parseInt(final String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }

    private static LocalDateTime parseDateTime(final String value) {
        try {
            return LocalDateTime.parse(value.trim(), GENERATED_FORMAT);
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid Generated at timestamp: " + value, ex);
        }
    }
}
