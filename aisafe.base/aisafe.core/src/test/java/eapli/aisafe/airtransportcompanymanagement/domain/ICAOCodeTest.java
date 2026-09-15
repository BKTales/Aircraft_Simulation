package eapli.aisafe.airtransportcompanymanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ICAOCodeTest {

    @Test
    void ensureIcaoCodeIsCreatedWithValidValue() {
        final ICAOCode twoLetters = ICAOCode.valueOf("tp");
        final ICAOCode threeLetters = ICAOCode.valueOf("tap");

        assertEquals("TP", twoLetters.toString());
        assertEquals("TAP", threeLetters.toString());
    }

    @Test
    void ensureIcaoCodeRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> ICAOCode.valueOf(null));
        assertThrows(IllegalArgumentException.class, () -> ICAOCode.valueOf(""));
        assertThrows(IllegalArgumentException.class, () -> ICAOCode.valueOf("T"));
        assertThrows(IllegalArgumentException.class, () -> ICAOCode.valueOf("TAPA"));
        assertThrows(IllegalArgumentException.class, () -> ICAOCode.valueOf("1TP"));
    }

    @Test
    void ensureEqualsHashCodeAndCompareTo() {
        final ICAOCode a = ICAOCode.valueOf("KLM");
        final ICAOCode b = ICAOCode.valueOf("klm");
        final ICAOCode c = ICAOCode.valueOf("TAP");

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.compareTo(c) < 0);
    }
}
