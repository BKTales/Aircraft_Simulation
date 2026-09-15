package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.domain.Temperature;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureTest {
    @Test
    void testTemperatureValues() {
        assertEquals(25.5, Temperature.valueOf(25.5).getTemperature());
        assertEquals(-40.0, Temperature.valueOf(-40.0).getTemperature());
        assertEquals(0.0, Temperature.valueOf(0).getTemperature());
    }

    @Test
    void testProtectedConstructorCoverage() {
        class ProtectedTemperature extends Temperature { ProtectedTemperature() { super(); } }
        assertNotNull(new ProtectedTemperature());
    }
}
