package eapli.aisafe.airtransportcompanymanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CompanyNameTest {

    @Test
    void ensureCompanyNameIsCreatedWithValidValue() {
        final CompanyName subject = CompanyName.valueOf("TAP Air Portugal");
        assertEquals("TAP Air Portugal", subject.toString());
    }

    @Test
    void ensureCompanyNameRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> CompanyName.valueOf(null));
        assertThrows(IllegalArgumentException.class, () -> CompanyName.valueOf(""));
        assertThrows(IllegalArgumentException.class, () -> CompanyName.valueOf("   "));
    }

    @Test
    void ensureEqualsAndHashCode() {
        final CompanyName a = CompanyName.valueOf("Lufthansa");
        final CompanyName b = CompanyName.valueOf("Lufthansa");
        final CompanyName c = CompanyName.valueOf("KLM");

        assertTrue(a.equals(a));
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertFalse(a.equals(null));
        assertFalse(a.equals("Lufthansa"));
        assertEquals(a.hashCode(), b.hashCode());
    }
}
