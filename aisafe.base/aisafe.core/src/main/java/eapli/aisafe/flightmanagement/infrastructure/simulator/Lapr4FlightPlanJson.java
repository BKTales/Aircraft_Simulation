package eapli.aisafe.flightmanagement.infrastructure.simulator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for LAPR4 client flight-plan JSON (numeric {@code ID}, {@code Segments} legs).
 */
public final class Lapr4FlightPlanJson {

    private static final Pattern NUMERIC_ID = Pattern.compile("\"ID\"\\s*:\\s*(\\d+)");

    private Lapr4FlightPlanJson() {}

    public static Optional<Integer> extractNumericId(final String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            return Optional.empty();
        }
        final Matcher matcher = NUMERIC_ID.matcher(jsonContent);
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }

    public static boolean hasSegmentPlan(final String jsonContent) {
        return jsonContent != null
                && jsonContent.contains("\"Aircraft\"")
                && jsonContent.contains("\"Segments\"")
                && !jsonContent.contains("\"Leg\": []");
    }

    public static boolean isSimulatorReady(final String jsonContent) {
        return jsonContent != null
                && jsonContent.contains("\"DepartureAirport\"")
                && jsonContent.contains("\"ModelId\"");
    }
}
