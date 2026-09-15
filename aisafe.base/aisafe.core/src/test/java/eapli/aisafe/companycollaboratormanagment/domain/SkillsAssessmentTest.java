package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.companycollaboratormanagment.application.InvalidSkillsAssessmentDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SkillsAssessmentTest {

    private static final LocalDate VALID_DATE = LocalDate.of(2024, 1, 1);

    @Test
    void ensureNullDateThrows() {
        assertThrows(IllegalArgumentException.class, () -> SkillsAssessment.valueOf(null));
    }

    @Test
    void ensureFutureDateThrows() {
        assertThrows(InvalidSkillsAssessmentDate.class, () -> SkillsAssessment.valueOf(LocalDate.now().plusDays(1)));
    }

    @Test
    void ensureTodayIsAccepted() {
        assertNotNull(SkillsAssessment.valueOf(LocalDate.now()));
    }

    @Test
    void ensureSameDateIsEqual() {
        final SkillsAssessment a1 = SkillsAssessment.valueOf(VALID_DATE);
        final SkillsAssessment a2 = SkillsAssessment.valueOf(VALID_DATE);
        assertEquals(a1, a2);
    }

    @Test
    void ensureSameDateHasSameHashCode() {
        final SkillsAssessment a1 = SkillsAssessment.valueOf(VALID_DATE);
        final SkillsAssessment a2 = SkillsAssessment.valueOf(VALID_DATE);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void ensureDifferentDateIsNotEqual() {
        final SkillsAssessment a1 = SkillsAssessment.valueOf(VALID_DATE);
        final SkillsAssessment a2 = SkillsAssessment.valueOf(VALID_DATE.plusDays(1));
        assertNotEquals(a1, a2);
    }

    @Test
    void ensureNotEqualToNull() {
        assertNotEquals(SkillsAssessment.valueOf(VALID_DATE), null);
    }

    @Test
    void ensureNotEqualToDifferentType() {
        assertNotEquals(SkillsAssessment.valueOf(VALID_DATE), "assessment");
    }

    @Test
    void ensureDateIsReturnedCorrectly() {
        assertEquals(VALID_DATE, SkillsAssessment.valueOf(VALID_DATE).date());
    }

    @Test
    void ensureIsValidWhenWithinFiveYears() {
        assertTrue(SkillsAssessment.valueOf(LocalDate.now().minusYears(4)).isValid());
    }

    @Test
    void ensureIsNotValidWhenOlderThanFiveYears() {
        assertFalse(SkillsAssessment.valueOf(LocalDate.now().minusYears(6)).isValid());
    }

    @Test
    void ensureToStringIsNotNull() {
        assertNotNull(SkillsAssessment.valueOf(VALID_DATE).toString());
    }

    @Test
    void ensureProtectedConstructorForORM() {
        final SkillsAssessment instance = new SkillsAssessment();
        assertNull(instance.date());
    }
}