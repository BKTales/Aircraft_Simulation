package eapli.aisafe.rcomp.protocol;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherRegisterPayloadTest {

    @Test
    void encodeDecodeRoundTrip() {
        final WeatherRegisterPayload.Fields original = new WeatherRegisterPayload.Fields(
                "AREA-0", 20f, 50f, 1013f, 180, 10f,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                List.of(new float[]{1f, 2f}, new float[]{3f, 4f}, new float[]{5f, 6f}));

        final WeatherRegisterPayload.Fields decoded =
                WeatherRegisterPayload.decode(WeatherRegisterPayload.encode(original));

        assertEquals(original.areaCode(), decoded.areaCode());
        assertEquals(original.temperature(), decoded.temperature());
        assertEquals(original.humidity(), decoded.humidity());
        assertEquals(original.pressure(), decoded.pressure());
        assertEquals(original.direction(), decoded.direction());
        assertEquals(original.speed(), decoded.speed());
        assertEquals(original.start(), decoded.start());
        assertEquals(original.end(), decoded.end());
        assertEquals(3, decoded.coordinates().size());
    }

    @Test
    void rejectsTooFewFields() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherRegisterPayload.decode("AREA-0;20;50"));
    }

    @Test
    void rejectsTooFewCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> WeatherRegisterPayload.encode(
                new WeatherRegisterPayload.Fields(
                        "AREA-0", 20f, 50f, 1013f, 180, 10f,
                        LocalDateTime.of(2026, 6, 1, 10, 0),
                        LocalDateTime.of(2026, 6, 1, 12, 0),
                        List.of(new float[]{1f, 1f}))));
    }

    @Test
    void rejectsInvalidCoordinatePair() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherRegisterPayload.decode(
                        "AREA-0;20;50;1013;180;10;01-06-2026 10:00;01-06-2026 12:00;1:2,invalid"));
    }

    @Test
    void rejectsBlankPayload() {
        assertThrows(IllegalArgumentException.class, () -> WeatherRegisterPayload.decode("  "));
    }

    @Test
    void rejectsInvalidDateTime() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherRegisterPayload.decode(
                        "AREA-0;20;50;1013;180;10;bad;01-06-2026 12:00;1:1,2:2,3:3"));
    }

    @Test
    void rejectsNullFieldsOnEncode() {
        assertThrows(IllegalArgumentException.class, () -> WeatherRegisterPayload.encode(null));
    }
}
