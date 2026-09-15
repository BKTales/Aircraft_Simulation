package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PilotUserCertificationTest {

    @Test
    void constructorRejectsNullAircraftModel() {
        final DueDate dueDate = DueDate.valueOf(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> new PilotCertification(null, dueDate));
    }

    @Test
    void constructorRejectsNullDueDate() {
        final AircraftModel model = mock(AircraftModel.class);

        assertThrows(IllegalArgumentException.class,
                () -> new PilotCertification(model, null));
    }

    @Test
    void aircraftModelGetterReturnsConstructorArgument() {
        final AircraftModel model = mock(AircraftModel.class);
        final PilotCertification subject = new PilotCertification(model, sampleDueDate());

        assertEquals(model, subject.aircraftModel());
    }

    @Test
    void dueDateGetterReturnsConstructorArgument() {
        final DueDate dueDate = sampleDueDate();
        final PilotCertification subject = new PilotCertification(mock(AircraftModel.class), dueDate);

        assertEquals(dueDate, subject.dueDate());
    }

    @Test
    void identityIsNotNull() {
        final PilotCertification subject = new PilotCertification(mock(AircraftModel.class), sampleDueDate());

        assertNotNull(subject.identity());
    }

    @Test
    void protectedConstructorForOrmDoesNotThrow() {
        assertNotNull(new PilotCertification());
    }

    @Test
    void sameAsReturnsTrueForSameInstance() {
        final PilotCertification reference = new PilotCertification(mock(AircraftModel.class), sampleDueDate());

        assertTrue(reference.sameAs(reference));
    }

    @Test
    void sameAsReturnsFalseForNull() {
        final PilotCertification reference = new PilotCertification(mock(AircraftModel.class), sampleDueDate());

        assertFalse(reference.sameAs(null));
    }

    @Test
    void sameAsReturnsFalseForDifferentType() {
        final PilotCertification reference = new PilotCertification(mock(AircraftModel.class), sampleDueDate());

        assertFalse(reference.sameAs("Not a certification"));
    }

    @Test
    void sameAsReturnsFalseForDifferentInstanceWithSameData() {
        final AircraftModel model = mock(AircraftModel.class);
        final PilotCertification reference = new PilotCertification(model, sampleDueDate());
        final PilotCertification other = new PilotCertification(model, sampleDueDate());

        assertFalse(reference.sameAs(other));
    }

    private static DueDate sampleDueDate() {
        return DueDate.valueOf(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
    }
}