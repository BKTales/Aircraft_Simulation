package eapli.aisafe.companycollaboratormanagment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DueDateTest {

    @Test
    void valueOfRejectsNullStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> DueDate.valueOf(null, LocalDate.of(2026, 1, 2)));
    }

    @Test
    void valueOfRejectsNullEndDate() {
        assertThrows(IllegalArgumentException.class,
                () -> DueDate.valueOf(LocalDate.of(2026, 1, 1), null));
    }

    @Test
    void valueOfRejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> DueDate.valueOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void startDateGetterReturnsConstructorArgument() {
        final DueDate subject = sampleDueDate();

        assertEquals(LocalDate.of(2026, 1, 1), subject.startDate());
    }

    @Test
    void endDateGetterReturnsConstructorArgument() {
        final DueDate subject = sampleDueDate();

        assertEquals(LocalDate.of(2026, 12, 31), subject.endDate());
    }

    @Test
    void protectedConstructorForOrmDoesNotThrow() {
        assertNotNull(new DueDate());
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        final DueDate reference = sampleDueDate();

        assertEquals(reference, reference);
    }

    @Test
    void equalsReturnsTrueForIdenticalValues() {
        assertEquals(sampleDueDate(), sampleDueDate());
    }

    @Test
    void equalsIsSymmetric() {
        final DueDate a = sampleDueDate();
        final DueDate b = sampleDueDate();

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equalsReturnsFalseForNull() {
        assertNotEquals(sampleDueDate(), null);
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        assertNotEquals("uma string qualquer", sampleDueDate());
    }

    @Test
    void equalsReturnsFalseWhenStartDateDiffers() {
        final DueDate reference = sampleDueDate();
        final DueDate differentStart = DueDate.valueOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 12, 31));

        assertNotEquals(reference, differentStart);
    }

    @Test
    void equalsReturnsFalseWhenEndDateDiffers() {
        final DueDate reference = sampleDueDate();
        final DueDate differentEnd = DueDate.valueOf(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 30));

        assertNotEquals(reference, differentEnd);
    }

    @Test
    void hashCodeIsEqualForIdenticalValues() {
        assertEquals(sampleDueDate().hashCode(), sampleDueDate().hashCode());
    }

    @Test
    void hashCodeDiffersForDifferentValues() {
        final DueDate reference = sampleDueDate();
        final DueDate different = DueDate.valueOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 12, 31));

        assertNotEquals(reference.hashCode(), different.hashCode());
    }

    private static DueDate sampleDueDate() {
        return DueDate.valueOf(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }
}