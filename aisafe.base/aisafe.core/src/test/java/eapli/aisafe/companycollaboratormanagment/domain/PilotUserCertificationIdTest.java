package eapli.aisafe.companycollaboratormanagment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PilotUserCertificationIdTest {

    @Test
    void newIdReturnsNonNullInstance() {
        assertNotNull(PilotCertificationId.newId());
    }

    @Test
    void newIdInternalValueIsNotNull() {
        assertNotNull(PilotCertificationId.newId().id());
    }

    @Test
    void newIdInternalValueIsNotBlank() {
        assertFalse(PilotCertificationId.newId().id().trim().isEmpty());
    }

    @Test
    void protectedConstructorForOrmDoesNotThrow() {
        assertNotNull(new PilotCertificationId());
    }


    @Test
    void equalsReturnsTrueForSameInstance() {
        final PilotCertificationId reference = PilotCertificationId.newId();

        assertEquals(reference, reference);
    }

    @Test
    void equalsReturnsFalseForNull() {
        final PilotCertificationId reference = PilotCertificationId.newId();

        assertNotEquals(reference, null);
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        final PilotCertificationId reference = PilotCertificationId.newId();

        assertNotEquals("Not id", reference);
    }


    @Test
    void hashCodeIsConsistentAcrossCallsOnSameInstance() {
        final PilotCertificationId reference = PilotCertificationId.newId();

        assertEquals(reference.hashCode(), reference.hashCode());
    }


    @Test
    void compareToReturnsZeroForSameInstance() {
        final PilotCertificationId id = PilotCertificationId.newId();

        assertEquals(0, id.compareTo(id));
    }

    @Test
    void compareToMatchesUnderlyingStringComparison() {
        final PilotCertificationId id1 = PilotCertificationId.newId();
        final PilotCertificationId id2 = PilotCertificationId.newId();

        assertEquals(id1.id().compareTo(id2.id()), id1.compareTo(id2));
    }
}