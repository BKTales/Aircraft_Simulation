package eapli.aisafe.rcomp.protocol;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherConsultPayloadTest {

    @Test
    void encodeDecodeRoundTrip() {
        final String payload = WeatherConsultPayload.encode("AREA-0", LocalDate.of(2026, 6, 14));
        final WeatherConsultPayload.Fields fields = WeatherConsultPayload.decode(payload);

        assertEquals("AREA-0", fields.areaCode());
        assertEquals(LocalDate.of(2026, 6, 14).atStartOfDay(), fields.day());
    }

    @Test
    void rejectsMissingDay() {
        assertThrows(IllegalArgumentException.class, () -> WeatherConsultPayload.decode("AREA-0"));
    }

    @Test
    void rejectsInvalidDate() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherConsultPayload.decode("AREA-0;99-99-2026"));
    }

    @Test
    void rejectsBlankAreaCodeOnEncode() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherConsultPayload.encode(" ", LocalDate.of(2026, 6, 1)));
    }

    @Test
    void rejectsBlankPayloadOnDecode() {
        assertThrows(IllegalArgumentException.class, () -> WeatherConsultPayload.decode(""));
    }
}
