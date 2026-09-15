package eapli.aisafe.flightmanagement.infrastructure.simulator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Technical adapter: extracts area codes from persisted simulator JSON without deserializing legs.
 */
public final class FlightPlanJsonAreaMatcher {

    private static final Pattern AREA_CODE = Pattern.compile("\"AreaCode\"\\s*:\\s*\"([^\"]+)\"");

    private FlightPlanJsonAreaMatcher() {
    }

    public static Optional<String> extractFirstAreaCode(final String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            return Optional.empty();
        }
        final Matcher areaMatcher = AREA_CODE.matcher(jsonContent);
        if (areaMatcher.find()) {
            return Optional.of(areaMatcher.group(1).trim());
        }
        return Optional.empty();
    }

    public static boolean matchesArea(final String jsonContent, final String areaCode) {
        if (jsonContent == null || jsonContent.isBlank() || areaCode == null || areaCode.isBlank()) {
            return false;
        }
        final String normalizedArea = areaCode.trim();
        final Matcher areaMatcher = AREA_CODE.matcher(jsonContent);
        while (areaMatcher.find()) {
            if (normalizedArea.equalsIgnoreCase(areaMatcher.group(1).trim())) {
                return true;
            }
        }
        return false;
    }
}
