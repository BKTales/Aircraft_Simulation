package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceSpecTest {

    @Test
    void ensureValidPerformanceSpecIsCreated() {
        double ceiling = 40000;
        double speed = 850;
        double fuel = 20000;
        double range = 5000;

        PerformanceSpec subject = PerformanceSpec.valueOf(ceiling, speed, fuel, range);

        assertNotNull(subject);
        assertEquals(ceiling, subject.serviceCeiling());
        assertEquals(speed, subject.cruiseSpeed());
        assertEquals(fuel, subject.fuelCapacity());
        assertEquals(range, subject.maxRange());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 850, 20000, 5000",   // serviceCeiling zero
            "40000, 0, 20000, 5000",   // cruiseSpeed zero
            "40000, 850, 0, 5000",     // fuelCapacity zero
            "40000, 850, 20000, 0",    // maxRange zero
            "-1, 850, 20000, 5000",    // serviceCeiling negativo
            "40000, -1, 20000, 5000",  // cruiseSpeed negativo
            "40000, 850, -1, 5000",    // fuelCapacity negativo
            "40000, 850, 20000, -1"    // maxRange negativo
    })
    void ensureInvalidValuesThrowException(double ceiling, double speed, double fuel, double range) {
        assertThrows(IllegalArgumentException.class, () ->
                PerformanceSpec.valueOf(ceiling, speed, fuel, range)
        );
    }

    @Test
    void testEqualsAndHashCode() {
        PerformanceSpec spec1 = PerformanceSpec.valueOf(40000, 850, 20000, 5000);
        PerformanceSpec spec2 = PerformanceSpec.valueOf(40000, 850, 20000, 5000);
        PerformanceSpec spec3 = PerformanceSpec.valueOf(30000, 850, 20000, 5000);

        // Identidade
        assertEquals(spec1, spec1);

        // Valores Iguais
        assertEquals(spec1, spec2);
        assertEquals(spec1.hashCode(), spec2.hashCode());

        // Valores Diferentes
        assertNotEquals(spec1, spec3);

        // Null e tipos diferentes
        assertNotEquals(spec1, null);
        assertNotEquals(spec1, "Performance");
    }

    @Test
    void testEqualsCoversEachDifferentField() {
        PerformanceSpec base = PerformanceSpec.valueOf(40000, 850, 20000, 5000);
        PerformanceSpec diffCeiling = PerformanceSpec.valueOf(39000, 850, 20000, 5000);
        PerformanceSpec diffCruise = PerformanceSpec.valueOf(40000, 840, 20000, 5000);
        PerformanceSpec diffFuel = PerformanceSpec.valueOf(40000, 850, 19999, 5000);
        PerformanceSpec diffRange = PerformanceSpec.valueOf(40000, 850, 20000, 4999);

        assertNotEquals(base, diffCeiling);
        assertNotEquals(base, diffCruise);
        assertNotEquals(base, diffFuel);
        assertNotEquals(base, diffRange);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        PerformanceSpec subject = new PerformanceSpec() {};
        assertNotNull(subject);
    }
}
