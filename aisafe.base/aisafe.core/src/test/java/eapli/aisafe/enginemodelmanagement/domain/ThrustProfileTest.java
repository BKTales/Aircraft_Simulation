package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ThrustProfileTest {

    @Test
    void ensureThrustProfileCalculatesLinearBehavior() {
        ThrustProfile subject = ThrustProfile.valueOf(100.0, 20.0);
        assertEquals(60.0, subject.thrustAtSpeed(400, 800), 0.001);
        assertEquals(20.0, subject.thrustAtSpeed(1000, 800), 0.001);
        assertEquals(100.0, subject.thrustAtStatic(), 0.000001);
        assertEquals(20.0, subject.thrustAtCruise(), 0.000001);
    }

    @Test
    void ensureConstructorValidatesThrust() {
        assertThrows(IllegalArgumentException.class, () -> ThrustProfile.valueOf(0, 20));
        assertThrows(IllegalArgumentException.class, () -> ThrustProfile.valueOf(100, 0));
        assertThrows(IllegalArgumentException.class, () -> ThrustProfile.valueOf(10, 20));
    }

    @Test
    void ensureEqualsAndHashCodeWork() {
        ThrustProfile p1 = ThrustProfile.valueOf(100, 20);
        ThrustProfile p2 = ThrustProfile.valueOf(100, 20);
        assertEquals(p1, p2);
        assertNotEquals(ThrustProfile.valueOf(100, 21), p1);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void ensureProtectedConstructorForJPACoverage() throws Exception {
        Constructor<ThrustProfile> constructor = ThrustProfile.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}