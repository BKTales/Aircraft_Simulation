package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NumberOfEnginesTest {

    @Test
    void ensureValidNumberOfEnginesIsCreated() {
        int expectedValue = 2;
        NumberOfEngines subject = NumberOfEngines.valueOf(expectedValue);

        assertEquals(expectedValue, subject.number());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void ensureNonPositiveValuesThrowException(int invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                NumberOfEngines.valueOf(invalidValue)
        );
    }

    @Test
    void testEqualsAndHashCode() {
        NumberOfEngines twins = NumberOfEngines.valueOf(2);
        NumberOfEngines twinsCopy = NumberOfEngines.valueOf(2);
        NumberOfEngines quads = NumberOfEngines.valueOf(4);

        // Mesma referência
        assertEquals(twins, twins);

        // Mesmos valores
        assertEquals(twins, twinsCopy);
        assertEquals(twins.hashCode(), twinsCopy.hashCode());

        // Valores diferentes
        assertNotEquals(twins, quads);

        // Comparação com null ou tipos diferentes
        assertNotEquals(twins, null);
        assertNotEquals(2, twins); // Inteiro puro vs Objeto de Domínio
    }

    @Test
    void testToString() {
        NumberOfEngines subject = NumberOfEngines.valueOf(4);
        assertEquals("4", subject.toString());
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        NumberOfEngines subject = new NumberOfEngines() {};
        assertNotNull(subject);
    }
}
