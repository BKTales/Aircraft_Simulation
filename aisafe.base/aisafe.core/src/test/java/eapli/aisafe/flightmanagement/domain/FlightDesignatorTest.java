package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class FlightDesignatorTest {

    @Test
    void ensureFlightDesignatorIsCreatedWithValidCode() {
        final String code = "TP1234A";
        final FlightDesignator subject = new FlightDesignator(code);
        assertEquals(code, subject.toString());
    }

    @Test
    void ensureConstructorThrowsExceptionForInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator(null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator(""));
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        final FlightDesignator d1 = new FlightDesignator("TP123");
        final FlightDesignator d2 = new FlightDesignator("TP123");
        final FlightDesignator d3 = new FlightDesignator("TP456");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertEquals(d1, d1);
        assertNotEquals(null, d1);

        assertNotEquals("Não é um FlightDesignator", d1);
    }

    @Test
    void ensureCompareToWorks() {
        final FlightDesignator a = new FlightDesignator("AAA");
        final FlightDesignator b = new FlightDesignator("BBB");

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new FlightDesignator("AAA")));
    }

    @Test
    void ensureCompareToHandlesNullCodesFromORM() throws Exception {
        Constructor<FlightDesignator> constructor = FlightDesignator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        FlightDesignator nullDesignator1 = constructor.newInstance();
        FlightDesignator nullDesignator2 = constructor.newInstance();
        FlightDesignator validDesignator = new FlightDesignator("AAA");

        assertEquals(0, nullDesignator1.compareTo(nullDesignator2));
        assertTrue(nullDesignator1.compareTo(validDesignator) < 0);
        assertTrue(validDesignator.compareTo(nullDesignator1) > 0);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<FlightDesignator> constructor = FlightDesignator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        FlightDesignator instance = constructor.newInstance();


        assertNotNull(instance);
        assertNull(instance.toString());
    }
}