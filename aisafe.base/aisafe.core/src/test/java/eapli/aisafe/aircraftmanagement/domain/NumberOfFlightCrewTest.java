package eapli.aisafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberOfFlightCrewTest {

    @Test
    void valueOfAcceptsPositive() {
        final NumberOfFlightCrew c = NumberOfFlightCrew.valueOf(4);
        assertEquals(4, c.count());
    }

    @Test
    void valueOfRejectsNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> NumberOfFlightCrew.valueOf(0));
        assertThrows(IllegalArgumentException.class, () -> NumberOfFlightCrew.valueOf(-1));
    }

    @Test
    void equalsAndHashCode() {
        final NumberOfFlightCrew a = NumberOfFlightCrew.valueOf(3);
        final NumberOfFlightCrew b = NumberOfFlightCrew.valueOf(3);
        final NumberOfFlightCrew c = NumberOfFlightCrew.valueOf(2);

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, "3");
        assertEquals(a.hashCode(), b.hashCode());
    }
}
