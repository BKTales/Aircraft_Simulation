package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.domain.Pressure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PressureTest {

    @Test
    void testValidPressureValues() {
        assertEquals(0.0, Pressure.valueOf(0).getPressure());
        assertEquals(1013.25, Pressure.valueOf(1013.25).getPressure());
        assertEquals(900.0, Pressure.valueOf(900).getPressure());
    }

    @Test
    void testNegativePressureThrows() {
        assertThrows(IllegalArgumentException.class, () -> Pressure.valueOf(-0.1));
        assertThrows(IllegalArgumentException.class, () -> Pressure.valueOf(-100));
    }

    @Test
    void testProtectedConstructorCoverage() {
        class ProtectedPressure extends Pressure { ProtectedPressure() { super(); } }
        assertNotNull(new ProtectedPressure());
    }
}
