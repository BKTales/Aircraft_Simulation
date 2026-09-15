package eapli.aisafe.rcomp.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherCsvPayloadTest {

    @Test
    void encodeDecodeRoundTrip() {
        final byte[] bytes = "area,temp\nAREA-0,20".getBytes(StandardCharsets.UTF_8);
        final String encoded = WeatherCsvPayload.encodeFile("bulk.csv", bytes);

        final WeatherCsvPayload.FileContent decoded = WeatherCsvPayload.decodeFile(encoded);

        assertEquals("bulk.csv", decoded.fileName());
        assertArrayEquals(bytes, decoded.bytes());
    }

    @Test
    void utf8Helper() {
        final byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);
        assertEquals("test", WeatherCsvPayload.utf8(bytes));
    }

    @Test
    void rejectsBlankFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherCsvPayload.encodeFile("  ", new byte[]{1}));
    }

    @Test
    void rejectsMalformedPayload() {
        assertThrows(IllegalArgumentException.class, () -> WeatherCsvPayload.decodeFile("only-name"));
    }

    @Test
    void rejectsNullContentOnEncode() {
        assertThrows(IllegalArgumentException.class,
                () -> WeatherCsvPayload.encodeFile("file.csv", null));
    }
}
