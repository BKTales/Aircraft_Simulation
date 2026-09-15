package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.companycollaboratormanagment.application.InvalidSecurityClearanceDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
class SecurityClearanceTest {

    private static final LocalDate VALID_DATE = LocalDate.of(2027, 4, 25);

    @Test
    void ensureThatNullDateThrows() {
        assertThrows(IllegalArgumentException.class, () -> SecurityClearance.valueOf(null));
    }

    @Test
    void ensurePastDateThrows() {
        assertThrows(InvalidSecurityClearanceDate.class, () -> SecurityClearance.valueOf(LocalDate.of(2020, 1, 1)));
    }

    @Test
    void ensureSameDateIsEqual() {
        final SecurityClearance c1 = SecurityClearance.valueOf(VALID_DATE);
        final SecurityClearance c2 = SecurityClearance.valueOf(VALID_DATE);
        assertEquals(c1, c2);
    }

    @Test
    void ensureSameDateHasSameHashCode() {
        final SecurityClearance c1 = SecurityClearance.valueOf(VALID_DATE);
        final SecurityClearance c2 = SecurityClearance.valueOf(VALID_DATE);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void ensureDifferentDateIsNotEqual() {
        final SecurityClearance c1 = SecurityClearance.valueOf(VALID_DATE);
        final SecurityClearance c2 = SecurityClearance.valueOf(VALID_DATE.plusDays(1));
        assertNotEquals(c1, c2);
    }

    @Test
    void ensureNotEqualToNull() {
        final SecurityClearance c1 = SecurityClearance.valueOf(VALID_DATE);
        assertNotEquals(c1, null);
    }

    @Test
    void ensureNotEqualToDifferentType() {
        final SecurityClearance c1 = SecurityClearance.valueOf(VALID_DATE);
        assertNotEquals(c1, "clearance");
    }

    @Test
    void ensureToStringIsNotNull() {
        assertNotNull(SecurityClearance.valueOf(VALID_DATE).toString());
    }

    @Test
    void ensureProtectedConstructorForORM() {
        final SecurityClearance instance = new SecurityClearance();
        assertNull(instance.expiryDate());
    }
}