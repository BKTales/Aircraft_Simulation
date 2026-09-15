package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MotorizationTypeTest {

    @Test
    void ensureFromLabelAcceptsValidValues_caseAndSpaces() {
        assertEquals(MotorizationType.TURBOFAN, MotorizationType.fromLabel("turbofan"));
        assertEquals(MotorizationType.TURBOFAN, MotorizationType.fromLabel("  TURBOFAN "));
        assertEquals(MotorizationType.ELECTRIC_PROPELLER, MotorizationType.fromLabel("Electric Propeller"));
    }

    @Test
    void ensureFromLabelRejectsBlankOrUnknown() {
        assertThrows(IllegalArgumentException.class, () -> MotorizationType.fromLabel(null));
        assertThrows(IllegalArgumentException.class, () -> MotorizationType.fromLabel(""));
        assertThrows(IllegalArgumentException.class, () -> MotorizationType.fromLabel("   "));
        assertThrows(IllegalArgumentException.class, () -> MotorizationType.fromLabel("rocket"));
    }

    @Test
    void ensureLabelIsNotBlank() {
        for (final MotorizationType t : MotorizationType.values()) {
            assertNotNull(t.label());
            assertFalse(t.label().isBlank());
        }
    }
}

