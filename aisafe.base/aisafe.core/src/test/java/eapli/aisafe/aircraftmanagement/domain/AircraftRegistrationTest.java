package eapli.aisafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AircraftRegistrationTest {

    @Test
    void valueOfCreatesNormalizedRegistration() {
        assertEquals("CS-VOF", AircraftRegistration.valueOf("  cs-vof  ").toString());
    }

    @Test
    void acceptsValidCodeAndNormalizes() {
        assertEquals("CS-DTX", new AircraftRegistration("  cs-dtx  ").toString());
        assertEquals("AB12C", new AircraftRegistration("ab12c").toString());
        assertEquals("ABC123", new AircraftRegistration("abc123").toString());
    }

    @Test
    void removesInternalWhitespaceInNormalization() {
        assertEquals("CS-TST", new AircraftRegistration("  cs - tst  ").toString());
    }

    @Test
    void compareToOrdersLexicographically() {
        final AircraftRegistration a = new AircraftRegistration("AA-AAA");
        final AircraftRegistration b = new AircraftRegistration("ZZ-ZZZ");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new AircraftRegistration("AA-AAA")));
    }

    @Test
    void rejectsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration(null));
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("   "));
    }

    @Test
    void rejectsTooShortOrTooLongOrInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("AB"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("ABCDEFGHIJKLMNO"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("CS@123"));
    }

    @Test
    void equalsHashCodeAndToString() {
        final AircraftRegistration r1 = new AircraftRegistration("CS-EQ1");
        final AircraftRegistration r2 = new AircraftRegistration("CS-EQ1");
        final AircraftRegistration r3 = new AircraftRegistration("CS-EQ2");

        assertEquals(r1, r1);
        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertNotEquals(r1, "CS-EQ1");
        assertFalse(r1.equals(null));
        assertEquals(r1.hashCode(), r2.hashCode());
        assertEquals("CS-EQ1", r1.toString());
    }

    @Test
    void protectedConstructorExistsForOrm() throws Exception {
        final var ctor = AircraftRegistration.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
