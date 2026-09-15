package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelIdTest {

    @Test
    void ensureValidAircraftModelIdIsCreated() {
        String code = "B737-800";
        AircraftModelId subject = AircraftModelId.valueOf(code);
        assertEquals(code, subject.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "}) // Espaços em branco se o framework não validar trim
    void ensureInvalidCodeThrowsException(String invalidCode) {
        // Testa as Preconditions de null e empty
        assertThrows(IllegalArgumentException.class, () -> AircraftModelId.valueOf(invalidCode));
    }

    @Test
    void testEqualsAndHashCode() {
        AircraftModelId id1 = AircraftModelId.valueOf("A320");
        AircraftModelId id2 = AircraftModelId.valueOf("A320");
        AircraftModelId id3 = AircraftModelId.valueOf("B777");

        // Reflexivo
        assertEquals(id1, id1);

        // Simétrico
        assertEquals(id1, id2);
        assertEquals(id2, id1);
        assertEquals(id1.hashCode(), id2.hashCode());

        // Diferente
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertNotEquals(id1, "A320");
    }

    @Test
    void testCompareTo() {
        AircraftModelId a = AircraftModelId.valueOf("A");
        AircraftModelId b = AircraftModelId.valueOf("B");
        AircraftModelId a2 = AircraftModelId.valueOf("A");

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(a2));
    }

    @Test
    void ensureCompareToThrowsOnNull() {
        AircraftModelId subject = AircraftModelId.valueOf("A320");
        assertThrows(IllegalArgumentException.class, () -> subject.compareTo(null));
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        AircraftModelId subject = new AircraftModelId() {};
        assertNotNull(subject);
    }
}
