package eapli.aisafe.rcomp.protocol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Payload for US043 remote consult: {@code areaCode;dd-MM-yyyy}. */
public final class WeatherConsultPayload {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public record Fields(String areaCode, LocalDateTime day) {}

    private WeatherConsultPayload() {}

    public static String encode(final String areaCode, final LocalDate day) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("areaCode is required.");
        }
        if (day == null) {
            throw new IllegalArgumentException("day is required.");
        }
        return areaCode.trim() + ";" + DAY.format(day);
    }

    public static Fields decode(final String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Expected: areaCode;dd-MM-yyyy");
        }
        final String[] parts = payload.split(";", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Expected: areaCode;dd-MM-yyyy");
        }
        try {
            return new Fields(parts[0].trim(), LocalDate.parse(parts[1].trim(), DAY).atStartOfDay());
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date. Use dd-MM-yyyy.");
        }
    }
}
