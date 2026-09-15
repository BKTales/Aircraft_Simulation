package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.domain.WeatherDate;
import eapli.aisafe.weatherdata.domain.WeatherSection;
import eapli.aisafe.weatherdata.domain.winddata.WindData;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Builds the weather snapshot JSON consumed by the C simulator ({@code FS_WEATHER_FILE}).
 */
public final class SimulatorWeatherSnapshotExporter {

    public String toJson(final String areaCode,
                         final List<WeatherData> areaRecords,
                         final List<Flight> flights) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }

        final Set<Long> seen = new LinkedHashSet<>();
        final List<WeatherData> merged = new ArrayList<>();
        if (areaRecords != null) {
            for (final WeatherData record : areaRecords) {
                if (record != null && record.identity() != null && seen.add(record.identity())) {
                    merged.add(record);
                }
            }
        }
        if (flights != null) {
            for (final Flight flight : flights) {
                final WeatherData attached = flight.weatherData();
                if (attached != null && attached.identity() != null && seen.add(attached.identity())) {
                    merged.add(attached);
                }
            }
        }

        final StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        sb.append("  \"areaCode\": ").append(q(areaCode)).append(",\n");
        sb.append("  \"records\": [");
        for (int i = 0; i < merged.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\n    ");
            appendRecord(sb, merged.get(i));
        }
        if (!merged.isEmpty()) {
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append('}');
        return sb.toString();
    }

    public List<WeatherData> resolveAreaRecords(final AreaCode areaCode,
                                                final LocalDateTime intervalStart,
                                                final LocalDateTime intervalEnd,
                                                final WeatherDataLookup lookup) {
        Objects.requireNonNull(areaCode, "areaCode");
        if (lookup == null) {
            return List.of();
        }
        return lookup.findByAreaAndInterval(areaCode, intervalStart, intervalEnd);
    }

    @FunctionalInterface
    public interface WeatherDataLookup {
        List<WeatherData> findByAreaAndInterval(AreaCode areaCode,
                                                LocalDateTime start,
                                                LocalDateTime end);
    }

    private static void appendRecord(final StringBuilder sb, final WeatherData data) {
        final WeatherDate dates = data.getWeatherDate();
        final WindData wind = data.getWindData();
        final WeatherSection section = data.getWeatherSection();
        final Bounds bounds = boundsOf(section != null ? section.getGeoBound() : null);

        sb.append("{\n");
        sb.append("      \"startTimeS\": ").append(toEpochSeconds(dates.getStartDateTime())).append(",\n");
        sb.append("      \"endTimeS\": ").append(toEpochSeconds(dates.getEndDateTime())).append(",\n");
        sb.append("      \"windDirectionDeg\": ")
                .append(wind.getDirection().getWindDirection()).append(",\n");
        sb.append("      \"windSpeedMs\": ")
                .append(trimDouble(wind.getSpeed().getSpeed())).append(",\n");
        sb.append("      \"bounds\": {\n");
        sb.append("        \"minLat\": ").append(trimDouble(bounds.minLat())).append(",\n");
        sb.append("        \"maxLat\": ").append(trimDouble(bounds.maxLat())).append(",\n");
        sb.append("        \"minLon\": ").append(trimDouble(bounds.minLon())).append(",\n");
        sb.append("        \"maxLon\": ").append(trimDouble(bounds.maxLon())).append("\n");
        sb.append("      }\n");
        sb.append("    }");
    }

    private static long toEpochSeconds(final LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    private static Bounds boundsOf(final GeographicBoundary boundary) {
        if (boundary == null || boundary.getGeoCords() == null || boundary.getGeoCords().isEmpty()) {
            return new Bounds(-90.0, 90.0, -180.0, 180.0);
        }
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        for (final GeographicCoords coord : boundary.getGeoCords()) {
            minLat = Math.min(minLat, coord.getX());
            maxLat = Math.max(maxLat, coord.getX());
            minLon = Math.min(minLon, coord.getY());
            maxLon = Math.max(maxLon, coord.getY());
        }
        return new Bounds(minLat, maxLat, minLon, maxLon);
    }

    private static String q(final String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String trimDouble(final double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private record Bounds(double minLat, double maxLat, double minLon, double maxLon) {
    }
}
