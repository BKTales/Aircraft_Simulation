package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AerodynamicCoefficientsTest {

    @Test
    void ensureCanCreateValidAerodynamicCoefficients() {
        final double cd0 = 0.02;
        final double cl = 1.5;
        AerodynamicCoefficients subject = AerodynamicCoefficients.valueOf(cd0, cl);

        assertNotNull(subject);
        assertEquals(cd0, subject.cd0());
        assertEquals(cl, subject.cl());
    }

    @Test
    void ensureNegativeCd0ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AerodynamicCoefficients.valueOf(-0.01, 1.0);
        });
    }

    @Test
    void ensureZeroCd0IsAllowed() {
        AerodynamicCoefficients subject = AerodynamicCoefficients.valueOf(0.0, 1.2);
        assertEquals(0.0, subject.cd0());
    }

    @Test
    void testEqualsSameObject() {
        AerodynamicCoefficients a = AerodynamicCoefficients.valueOf(0.02, 1.5);
        assertEquals(a, a);
    }

    @Test
    void testEqualsDifferentObjectsSameValues() {
        AerodynamicCoefficients a = AerodynamicCoefficients.valueOf(0.02, 1.5);
        AerodynamicCoefficients b = AerodynamicCoefficients.valueOf(0.02, 1.5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testNotEqualsDifferentValues() {
        AerodynamicCoefficients a = AerodynamicCoefficients.valueOf(0.02, 1.5);
        AerodynamicCoefficients b = AerodynamicCoefficients.valueOf(0.03, 1.5);
        AerodynamicCoefficients c = AerodynamicCoefficients.valueOf(0.02, 1.6);

        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEqualsNullAndOtherClass() {
        AerodynamicCoefficients a = AerodynamicCoefficients.valueOf(0.02, 1.5);
        assertNotEquals(null, a);
        assertNotEquals("not a coefficient", a);
    }

    @Test
    void ensureProtectedConstructorExistsForJPA() {

        AerodynamicCoefficients subject = new AerodynamicCoefficients() {};
        assertNotNull(subject);
    }

    @ParameterizedTest
    @CsvSource({
            "0.02, 1.5, 0.02, 1.5, true",
            "0.02, 1.5, 0.03, 1.5, false",
            "0.02, 1.5, 0.02, 1.6, false"
    })
    void testEqualityLogic(double cd01, double cl1, double cd02, double cl2, boolean expected) {
        AerodynamicCoefficients a = AerodynamicCoefficients.valueOf(cd01, cl1);
        AerodynamicCoefficients b = AerodynamicCoefficients.valueOf(cd02, cl2);
        assertEquals(expected, a.equals(b));
    }
}