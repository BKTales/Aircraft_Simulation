package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ModelNameTest {

    @Test
    void ensureValidModelNameIsCreated() {
        String validName = "Airbus A320neo";
        ModelName subject = ModelName.valueOf(validName);

        assertEquals(validName, subject.name());
        assertEquals(validName, subject.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " "})
    void ensureInvalidNamesThrowException(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> ModelName.valueOf(invalidName));
    }

    @Test
    void testEqualsAndHashCode() {
        ModelName name1 = ModelName.valueOf("Boeing 737");
        ModelName name2 = ModelName.valueOf("Boeing 737");
        ModelName name3 = ModelName.valueOf("Cessna 172");

        // Identidade
        assertEquals(name1, name1);

        // Igualdade por valor
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());

        // Diferença
        assertNotEquals(name1, name3);
        assertNotEquals(name1, null);
        assertNotEquals(name1, "Boeing 737"); // Tipos diferentes
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        ModelName subject = new ModelName() {};
        assertNotNull(subject);
    }
}