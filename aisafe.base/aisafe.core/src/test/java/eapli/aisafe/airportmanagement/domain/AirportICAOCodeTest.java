package eapli.aisafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportICAOCodeTest {

    @Test
    void ensureValidAlphabeticCodeIsAccepted() {
        final AirportICAOCode code = AirportICAOCode.valueOf("LPPT");
        assertEquals("LPPT", code.toString());
    }

    @Test
    void ensureValidAlphanumericCodeIsAccepted() {
        final AirportICAOCode code = AirportICAOCode.valueOf("K1LA");
        assertEquals("K1LA", code.toString());
    }

    @Test
    void ensureLowercaseInputIsNormalisedToUppercase() {
        final AirportICAOCode code = AirportICAOCode.valueOf("lppt");
        assertEquals("LPPT", code.toString());
    }

    @Test
    void ensureInputWithSurroundingWhitespaceIsTrimmed() {
        final AirportICAOCode code = AirportICAOCode.valueOf("  LPPR  ");
        assertEquals("LPPR", code.toString());
    }

    @Test
    void ensureNullInputThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportICAOCode.valueOf(null);
            }
        });
    }

    @Test
    void ensureBlankInputThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportICAOCode.valueOf("   ");
            }
        });
    }

    @Test
    void ensureThreeCharacterCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportICAOCode.valueOf("LIS");
            }
        });
    }

    @Test
    void ensureFiveCharacterCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportICAOCode.valueOf("LPPPT");
            }
        });
    }

    @Test
    void ensureCodeWithSpecialCharactersThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportICAOCode.valueOf("LP-T");
            }
        });
    }

    @Test
    void ensureEqualCodesAreEqual() {
        final AirportICAOCode a = AirportICAOCode.valueOf("LPPT");
        final AirportICAOCode b = AirportICAOCode.valueOf("lppt");
        assertEquals(a, b);
    }

    @Test
    void ensureDifferentCodesAreNotEqual() {
        final AirportICAOCode a = AirportICAOCode.valueOf("LPPT");
        final AirportICAOCode b = AirportICAOCode.valueOf("LPPR");
        assertNotEquals(a, b);
    }

    @Test
    void ensureEqualCodesHaveSameHashCode() {
        final AirportICAOCode a = AirportICAOCode.valueOf("LPFR");
        final AirportICAOCode b = AirportICAOCode.valueOf("lpfr");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureCompareToReturnsZeroForEqualCodes() {
        final AirportICAOCode a = AirportICAOCode.valueOf("LPPT");
        final AirportICAOCode b = AirportICAOCode.valueOf("LPPT");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void ensureCompareToIsConsistentWithAlphabeticOrder() {
        final AirportICAOCode a = AirportICAOCode.valueOf("LPFR");
        final AirportICAOCode b = AirportICAOCode.valueOf("LPPT");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }
}
