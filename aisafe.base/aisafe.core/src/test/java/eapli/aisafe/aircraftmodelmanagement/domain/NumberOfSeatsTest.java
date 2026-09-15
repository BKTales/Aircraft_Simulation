package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NumberOfSeatsTest {

    @Test
    void ensureValidNumberOfSeatsIsCreated() {
        final int expected = 180;
        final NumberOfSeats subject = NumberOfSeats.valueOf(expected);

        assertEquals(expected, subject.seats());
    }

    @Test
    void ensureZeroSeatsIsAllowed() {
        final NumberOfSeats subject = NumberOfSeats.valueOf(0);
        assertEquals(0, subject.seats());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void ensureNegativeValuesThrowException(final int invalidValue) {
        assertThrows(IllegalArgumentException.class, () -> NumberOfSeats.valueOf(invalidValue));
    }

    @Test
    void testEqualsAndHashCode() {
        final NumberOfSeats a = NumberOfSeats.valueOf(150);
        final NumberOfSeats aCopy = NumberOfSeats.valueOf(150);
        final NumberOfSeats b = NumberOfSeats.valueOf(151);

        assertEquals(a, a);
        assertEquals(a, aCopy);
        assertEquals(a.hashCode(), aCopy.hashCode());
        assertNotEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(150, a);
    }

    @Test
    void testToString() {
        assertEquals("42", NumberOfSeats.valueOf(42).toString());
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        final NumberOfSeats subject = new NumberOfSeats() {};
        assertNotNull(subject);
    }
}
