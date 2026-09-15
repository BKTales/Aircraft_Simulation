package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class TSFCTest {

    @Test
    void ensureTSFCIsCreatedWithPositiveValue() {
        TSFC subject = TSFC.valueOf(0.5);
        assertNotNull(subject);
        assertEquals(0.5, subject.value(), 0.000001);
    }

    @Test
    void ensureConstructorThrowsExceptionForNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> TSFC.valueOf(0));
        assertThrows(IllegalArgumentException.class, () -> TSFC.valueOf(-0.1));
    }

    @Test
    void ensureEqualsAndHashCodeWork() {
        TSFC t1 = TSFC.valueOf(0.5);
        TSFC t2 = TSFC.valueOf(0.5);
        TSFC t3 = TSFC.valueOf(0.6);

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void ensureProtectedConstructorForJPACoverage() throws Exception {
        Constructor<TSFC> constructor = TSFC.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

}