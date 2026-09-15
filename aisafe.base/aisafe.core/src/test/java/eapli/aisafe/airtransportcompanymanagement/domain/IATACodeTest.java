package eapli.aisafe.airtransportcompanymanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IATACodeTest {

    @Test
    void ensureIataCodeIsCreatedWithValidValue() {
        final IATACode subject = IATACode.valueOf("tp");
        assertEquals("TP", subject.toString());
    }

    @Test
    void ensureIataCodeRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf(null));
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf(""));
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf("T"));
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf("TAP"));
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf("1A"));
        assertThrows(IllegalArgumentException.class, () -> IATACode.valueOf("A1"));
    }

    @Test
    void ensureEqualsHashCodeAndCompareTo() {
        final IATACode a = IATACode.valueOf("AF");
        final IATACode b = IATACode.valueOf("af");
        final IATACode c = IATACode.valueOf("TP");

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.compareTo(c) < 0);
    }
}
