package eapli.aisafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportIATACodeTest {

    @Test
    void ensureValidCodeIsAccepted() {
        final AirportIATACode code = AirportIATACode.valueOf("LIS");
        assertEquals("LIS", code.toString());
    }

    @Test
    void ensureLowercaseInputIsNormalisedToUppercase() {
        final AirportIATACode code = AirportIATACode.valueOf("lis");
        assertEquals("LIS", code.toString());
    }

    @Test
    void ensureMixedCaseInputIsNormalisedToUppercase() {
        final AirportIATACode code = AirportIATACode.valueOf("lIs");
        assertEquals("LIS", code.toString());
    }

    @Test
    void ensureInputWithSurroundingWhitespaceIsTrimmed() {
        final AirportIATACode code = AirportIATACode.valueOf("  FAO  ");
        assertEquals("FAO", code.toString());
    }

    @Test
    void ensureNullInputThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf(null);
            }
        });
    }

    @Test
    void ensureBlankInputThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf("   ");
            }
        });
    }

    @Test
    void ensureTwoLetterCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf("TP");
            }
        });
    }

    @Test
    void ensureFourLetterCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf("LPPT");
            }
        });
    }

    @Test
    void ensureNumericCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf("1AB");
            }
        });
    }

    @Test
    void ensureCodeWithSpecialCharactersThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                AirportIATACode.valueOf("L-S");
            }
        });
    }

    @Test
    void ensureEqualCodesAreEqual() {
        final AirportIATACode a = AirportIATACode.valueOf("LIS");
        final AirportIATACode b = AirportIATACode.valueOf("lis");
        assertEquals(a, b);
    }

    @Test
    void ensureDifferentCodesAreNotEqual() {
        final AirportIATACode a = AirportIATACode.valueOf("LIS");
        final AirportIATACode b = AirportIATACode.valueOf("OPO");
        assertNotEquals(a, b);
    }

    @Test
    void ensureEqualCodesHaveSameHashCode() {
        final AirportIATACode a = AirportIATACode.valueOf("FAO");
        final AirportIATACode b = AirportIATACode.valueOf("fao");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureCompareToReturnsZeroForEqualCodes() {
        final AirportIATACode a = AirportIATACode.valueOf("LIS");
        final AirportIATACode b = AirportIATACode.valueOf("LIS");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void ensureCompareToIsConsistentWithAlphabeticOrder() {
        final AirportIATACode a = AirportIATACode.valueOf("FAO");
        final AirportIATACode b = AirportIATACode.valueOf("LIS");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }
}
