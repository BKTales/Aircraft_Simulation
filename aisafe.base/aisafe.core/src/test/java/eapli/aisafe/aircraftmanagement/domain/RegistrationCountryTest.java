package eapli.aisafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationCountryTest {

    @Test
    void valueOfCreatesNormalizedCountry() {
        assertEquals("PT", RegistrationCountry.valueOf(" pt ").isoAlpha2());
    }

    @Test
    void normalizesToUpperAlpha2() {
        final RegistrationCountry pt = new RegistrationCountry(" pt ");
        assertEquals("PT", pt.isoAlpha2());
        assertEquals("PT", pt.toString());
    }

    @Test
    void rejectsNullBlankAndInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationCountry(null));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationCountry(" "));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationCountry("PRT"));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationCountry("P1"));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationCountry("p"));
    }

    @Test
    void equalsAndHashCode() {
        final RegistrationCountry a = new RegistrationCountry("ES");
        final RegistrationCountry b = new RegistrationCountry("es");
        final RegistrationCountry c = new RegistrationCountry("FR");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, "ES");
        assertFalse(a.equals(null));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void protectedConstructorExistsForOrm() throws Exception {
        final var ctor = RegistrationCountry.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
