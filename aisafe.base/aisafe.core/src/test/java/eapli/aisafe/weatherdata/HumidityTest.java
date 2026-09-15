package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.application.exceptions.InvalidHumidityValueException;
import eapli.aisafe.weatherdata.domain.Humidity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HumidityTest {
    @Test
    void testValidHumidity() {
        assertEquals(55.5, Humidity.valueOf(55.5).getHumidity());
        assertEquals(0.0, Humidity.valueOf(0).getHumidity());
        assertEquals(100.0, Humidity.valueOf(100).getHumidity());
    }

    @Test
    void testInvalidHumidity() {
        assertThrows(InvalidHumidityValueException.class, () -> Humidity.valueOf(101));
        assertThrows(InvalidHumidityValueException.class, () -> Humidity.valueOf(-1));
    }

    @Test
    void testProtectedConstructorCoverage() {
        class ProtectedHumidity extends Humidity { ProtectedHumidity() { super(); } }
        assertNotNull(new ProtectedHumidity());
    }
}
