package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class WeightSpecificationTest {

    @Test
    void ensureValidWeightSpecificationIsCreated() {
        double mtow = 70000;
        double mzfw = 60000;
        double empty = 40000;

        WeightSpecification subject = WeightSpecification.valueOf(mtow, mzfw, empty);

        assertNotNull(subject);
        assertEquals(mtow, subject.mtow());
        assertEquals(mzfw, subject.mzfw());
        assertEquals(empty, subject.emptyWeight());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 60000, 40000",   // mtow zero
            "70000, 0, 40000",   // mzfw zero
            "70000, 60000, 0",   // empty zero
            "-1, 60000, 40000",  // mtow negativo
            "70000, -1, 40000",  // mzfw negativo
            "70000, 60000, -1"   // empty negativo
    })
    void ensureNonPositiveWeightsThrowException(double mtow, double mzfw, double empty) {
        assertThrows(IllegalArgumentException.class, () ->
                WeightSpecification.valueOf(mtow, mzfw, empty)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "60000, 60000, 40000", // MTOW == MZFW (viola MTOW > MZFW)
            "50000, 60000, 40000", // MTOW < MZFW
            "70000, 40000, 40000", // MZFW == Empty (viola MZFW > Empty)
            "70000, 30000, 40000"  // MZFW < Empty
    })
    void ensureInconsistentWeightsThrowException(double mtow, double mzfw, double empty) {
        // Testa a segunda regra de validação (a hierarquia de pesos)
        assertThrows(IllegalArgumentException.class, () ->
                WeightSpecification.valueOf(mtow, mzfw, empty)
        );
    }

    @Test
    void testEqualsAndHashCode() {
        WeightSpecification w1 = WeightSpecification.valueOf(70, 60, 40);
        WeightSpecification w2 = WeightSpecification.valueOf(70, 60, 40);
        WeightSpecification w3 = WeightSpecification.valueOf(80, 60, 40);

        // Identidade
        assertEquals(w1, w1);

        // Igualdade por valor
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());

        // Diferença
        assertNotEquals(w1, w3);
        assertNotEquals(w1, null);
        assertNotEquals("Weight", w1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        WeightSpecification subject = new WeightSpecification() {};
        assertNotNull(subject);
    }
}
