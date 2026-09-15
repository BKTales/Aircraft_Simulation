package eapli.aisafe.flightmanagement.infrastructure.reporting;

import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorFlightDesignatorMap;

import java.util.Map;

/**
 * Replaces simulator numeric flight IDs with designators in archived US109 report files.
 */
public final class SimulationReportDesignatorRewriter {

    private enum Section {
        OTHER,
        FLIGHTS,
        VIOLATIONS
    }

    private SimulationReportDesignatorRewriter() {}

    public static String rewriteCsv(final String content, final Map<String, String> designatorBySimulatorId) {
        if (content == null || content.isBlank()
                || designatorBySimulatorId == null || designatorBySimulatorId.isEmpty()) {
            return content;
        }
        final StringBuilder out = new StringBuilder(content.length());
        Section section = Section.OTHER;
        for (final String rawLine : content.split("\n", -1)) {
            final String line = rawLine.trim();
            if (line.startsWith("flight_id_a,flight_id_b,violation_type")) {
                section = Section.VIOLATIONS;
                out.append(rawLine).append('\n');
                continue;
            }
            if (line.startsWith("flight_id,") && line.contains("execution_status")) {
                section = Section.FLIGHTS;
                out.append(rawLine).append('\n');
                continue;
            }
            out.append(switch (section) {
                case FLIGHTS -> replaceLeadingCsvFields(rawLine, designatorBySimulatorId, 1);
                case VIOLATIONS -> replaceLeadingCsvFields(rawLine, designatorBySimulatorId, 2);
                default -> rawLine;
            }).append('\n');
        }
        if (!content.endsWith("\n") && out.length() > 0) {
            out.setLength(out.length() - 1);
        }
        return out.toString();
    }

    public static String rewriteTxt(final String content, final Map<String, String> designatorBySimulatorId) {
        if (content == null || content.isBlank()
                || designatorBySimulatorId == null || designatorBySimulatorId.isEmpty()) {
            return content;
        }
        String rewritten = content;
        for (final Map.Entry<String, String> entry : designatorBySimulatorId.entrySet()) {
            final String simulatorId = entry.getKey();
            final String designator = entry.getValue();
            rewritten = rewritten.replace("Flight ID " + simulatorId, "Flight ID " + designator);
            rewritten = rewritten.replace("(ID " + simulatorId + ")", "(ID " + designator + ")");
        }
        return rewritten;
    }

    private static String replaceLeadingCsvFields(final String line,
                                                  final Map<String, String> designatorBySimulatorId,
                                                  final int fieldsToReplace) {
        if (line.isBlank()) {
            return line;
        }
        final String[] parts = splitCsvLine(line);
        if (parts.length < fieldsToReplace) {
            return line;
        }
        for (int i = 0; i < fieldsToReplace; i++) {
            parts[i] = SimulatorFlightDesignatorMap.resolve(designatorBySimulatorId, parts[i]);
        }
        return String.join(",", parts);
    }

    private static String[] splitCsvLine(final String line) {
        final java.util.List<String> parts = new java.util.ArrayList<>();
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (c == ',') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(String[]::new);
    }
}
