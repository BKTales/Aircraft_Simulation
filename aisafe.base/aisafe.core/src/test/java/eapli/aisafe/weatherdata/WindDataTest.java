package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.domain.winddata.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindDataTest {
    @Test
    void testWindNormalization() {
        assertEquals(180, WindDataDirection.valueOf(180).getWindDirection());
        assertEquals(90, WindDataDirection.valueOf(450).getWindDirection());
        assertEquals(0, WindDataDirection.valueOf(720).getWindDirection());
        assertEquals(20, WindDataDirection.valueOf(-20).getWindDirection());
    }

    @Test
    void testWindSpeedNegative() {
        assertThrows(IllegalArgumentException.class, () -> WindDataSpeed.valueOf(-5));
    }

    @Test
    void testWindDataSpeedValidAndComposition() {
        WindDataDirection direction = WindDataDirection.valueOf(270);
        WindDataSpeed speed = WindDataSpeed.valueOf(15.5);
        WindData data = WindData.valueOf(direction, speed);

        assertEquals(15.5, data.getSpeed().getSpeed());
        assertEquals(270, data.getDirection().getWindDirection());
    }

    @Test
    void testProtectedConstructorsCoverage() {
        class ProtectedDirection extends WindDataDirection { ProtectedDirection() { super(); } }
        class ProtectedSpeed extends WindDataSpeed { ProtectedSpeed() { super(); } }
        class ProtectedWindData extends WindData { ProtectedWindData() { super(); } }

        assertNotNull(new ProtectedDirection());
        assertNotNull(new ProtectedSpeed());
        assertNotNull(new ProtectedWindData());
    }
}
