package eapli.aisafe.rcomp.protocol;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload for US041 remote registration:
 * {@code areaCode;temp;hum;press;dir;speed;start;end;x1:y1,x2:y2,...}
 * Dates use {@code dd-MM-yyyy HH:mm}.
 */
public final class WeatherRegisterPayload {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public record Fields(
            String areaCode,
            float temperature,
            float humidity,
            float pressure,
            int direction,
            float speed,
            LocalDateTime start,
            LocalDateTime end,
            List<float[]> coordinates) {}

    private WeatherRegisterPayload() {}

    public static String encode(final Fields fields) {
        if (fields == null) {
            throw new IllegalArgumentException("fields are required.");
        }
        if (fields.coordinates() == null || fields.coordinates().size() < 3) {
            throw new IllegalArgumentException("At least 3 boundary points are required.");
        }
        final StringBuilder coords = new StringBuilder();
        for (int i = 0; i < fields.coordinates().size(); i++) {
            if (i > 0) {
                coords.append(',');
            }
            final float[] point = fields.coordinates().get(i);
            coords.append(point[0]).append(':').append(point[1]);
        }
        return String.join(";",
                fields.areaCode().trim(),
                Float.toString(fields.temperature()),
                Float.toString(fields.humidity()),
                Float.toString(fields.pressure()),
                Integer.toString(fields.direction()),
                Float.toString(fields.speed()),
                DATE_TIME.format(fields.start()),
                DATE_TIME.format(fields.end()),
                coords.toString());
    }

    public static Fields decode(final String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Expected: areaCode;temp;hum;press;dir;speed;start;end;x1:y1,x2:y2,...");
        }
        final String[] parts = payload.split(";", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException(
                    "Expected: areaCode;temp;hum;press;dir;speed;start;end;x1:y1,x2:y2,...");
        }
        try {
            return new Fields(
                    parts[0].trim(),
                    Float.parseFloat(parts[1].trim()),
                    Float.parseFloat(parts[2].trim()),
                    Float.parseFloat(parts[3].trim()),
                    Integer.parseInt(parts[4].trim()),
                    Float.parseFloat(parts[5].trim()),
                    LocalDateTime.parse(parts[6].trim(), DATE_TIME),
                    LocalDateTime.parse(parts[7].trim(), DATE_TIME),
                    parseCoordinates(parts[8]));
        } catch (final NumberFormatException | DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid register weather payload: " + ex.getMessage());
        }
    }

    private static List<float[]> parseCoordinates(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Boundary coordinates are required.");
        }
        final String[] points = raw.split(",");
        if (points.length < 3) {
            throw new IllegalArgumentException("At least 3 boundary points are required.");
        }
        final List<float[]> coordinates = new ArrayList<>();
        for (final String point : points) {
            final String[] xy = point.trim().split(":");
            if (xy.length != 2) {
                throw new IllegalArgumentException("Invalid coordinate pair: " + point);
            }
            coordinates.add(new float[]{
                    Float.parseFloat(xy[0].trim()),
                    Float.parseFloat(xy[1].trim())
            });
        }
        return coordinates;
    }
}
