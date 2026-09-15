package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.SafetyViolationEvent;
import eapli.aisafe.flightmanagement.domain.simulation.ScheduleFlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.ValidationStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SimulationReportParser {

    private enum Section {
        METRICS,
        COLLISION_FLIGHTS,
        FLIGHTS,
        VIOLATIONS
    }

    public FlightSimulationReport parse(final Path csvPath, final AreaCode areaCode) throws IOException {
        if (csvPath == null || areaCode == null) {
            throw new IllegalArgumentException("CSV path and area code are required.");
        }
        final List<String> lines = Files.readAllLines(csvPath);
        ValidationStatus validationResult = ValidationStatus.FAIL;
        final List<FlightStatus> flightStatuses = new ArrayList<>();
        final List<ScheduleFlightStatus> scheduleFlightStatuses = new ArrayList<>();
        final List<SafetyViolationEvent> safetyViolations = new ArrayList<>();
        Section section = Section.METRICS;

        for (final String rawLine : lines) {
            final String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            if ("collision_flights".equals(line)) {
                section = Section.COLLISION_FLIGHTS;
                continue;
            }

            if (line.startsWith("flight_id_a,flight_id_b,violation_type")) {
                section = Section.VIOLATIONS;
                continue;
            }

            if (line.startsWith("flight_id,") && line.contains("departure_utc")) {
                section = Section.FLIGHTS;
                continue;
            }

            if (line.startsWith("flight_id,") && line.contains("execution_status") && section != Section.COLLISION_FLIGHTS) {
                section = Section.FLIGHTS;
                continue;
            }

            switch (section) {
                case METRICS -> {
                    final String[] metric = splitCsvLine(line, 2);
                    if (metric.length == 2 && "validation_result".equals(metric[0])) {
                        validationResult = ValidationStatus.fromReportValue(metric[1]);
                    }
                }
                case COLLISION_FLIGHTS -> {
                    // skip collision-only rows until blank line advances section
                }
                case FLIGHTS -> parseFlightLine(line, flightStatuses, scheduleFlightStatuses);
                case VIOLATIONS -> parseViolationLine(line, safetyViolations);
            }
        }

        return new FlightSimulationReport(
                areaCode,
                validationResult,
                flightStatuses,
                scheduleFlightStatuses,
                safetyViolations);
    }

    private static void parseFlightLine(final String line,
                                        final List<FlightStatus> flightStatuses,
                                        final List<ScheduleFlightStatus> scheduleFlightStatuses) {
        final String[] cols = splitCsvLine(line, -1);
        if (cols.length < 4) {
            return;
        }
        final String flightId = cols[0];
        if (!isNumeric(flightId)) {
            return;
        }
        final FlightExecutionStatus executionStatus = FlightExecutionStatus.fromReportValue(cols[3]);
        flightStatuses.add(new FlightStatus(flightId, executionStatus));
        scheduleFlightStatuses.add(new ScheduleFlightStatus(
                flightId,
                executionStatus == FlightExecutionStatus.SUCCESS ? ValidationStatus.PASS : ValidationStatus.FAIL
        ));
    }

    private static void parseViolationLine(final String line, final List<SafetyViolationEvent> violations) {
        final String[] cols = splitCsvLine(line, -1);
        if (cols.length < 17) {
            return;
        }
        if (!isNumeric(cols[0]) || !isNumeric(cols[1])) {
            return;
        }
        final int step = parseInt(cols[3]);
        final int elapsed = parseInt(cols[4]);
        final String positionA = formatPosition(cols[7], cols[8], cols[9]);
        final String positionB = formatPosition(cols[12], cols[13], cols[14]);
        violations.add(new SafetyViolationEvent(
                cols[2],
                step,
                elapsed,
                cols[0],
                positionA,
                cols[1],
                positionB));
    }

    private static String formatPosition(final String lat, final String lon, final String alt) {
        return String.format("lat %s, lon %s, alt %s m", lat, lon, alt);
    }

    private static boolean isNumeric(final String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static int parseInt(final String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String[] splitCsvLine(final String line, final int maxParts) {
        final List<String> parts = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (c == ',') {
                parts.add(current.toString());
                current.setLength(0);
                if (maxParts > 0 && parts.size() == maxParts - 1) {
                    parts.add(line.substring(i + 1));
                    break;
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0 || line.endsWith(",")) {
            parts.add(current.toString());
        }
        return parts.toArray(String[]::new);
    }
}
