package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class FlightLoadTest {

    @Test
    void ensureFlightLoadIsCreatedWithValidValues() {
        FlightLoad subject = new FlightLoad(150, 12000.0, 1200.50);

        assertEquals(150, subject.passengerCount());
        assertEquals(12000.0, subject.passengerWeight());
        assertEquals(1200.50, subject.cargoWeight());
        assertEquals(13200.50, subject.totalPayloadMass());
    }

    @Test
    void ensureConstructorThrowsExceptionForNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new FlightLoad(-1, 12000.0, 1000.0));
        assertThrows(IllegalArgumentException.class, () -> new FlightLoad(100, -1.0, 1000.0));
        assertThrows(IllegalArgumentException.class, () -> new FlightLoad(100, 12000.0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new FlightLoad(-5, -500.0, -500.0));
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        FlightLoad load1 = new FlightLoad(100, 8000.0, 500.0);
        FlightLoad load2 = new FlightLoad(100, 8000.0, 500.0);

        FlightLoad load3 = new FlightLoad(200, 8000.0, 500.0);
        FlightLoad load4 = new FlightLoad(100, 9000.0, 500.0);
        FlightLoad load5 = new FlightLoad(100, 8000.0, 600.0);


        assertEquals(load1, load2);
        assertEquals(load1.hashCode(), load2.hashCode());

        assertNotEquals(load1, load3);
        assertNotEquals(load1, load4);
        assertNotEquals(load1, load5);
        assertEquals(load1, load1);
        assertNotEquals(null, load1);
        assertNotEquals("Não é um FlightLoad", load1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<FlightLoad> constructor = FlightLoad.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        FlightLoad instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(0, instance.passengerCount());
        assertEquals(0.0, instance.cargoWeight());
    }
}