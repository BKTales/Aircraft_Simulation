package eapli.aisafe.aircraftmodelmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class WingGeometryTest {

    @Test
    void ensureValidWingGeometryIsCreated() {
        double area = 125.0;
        double span = 34.0;
        WingGeometry subject = WingGeometry.valueOf(area, span);

        assertNotNull(subject);
        assertEquals(area, subject.wingArea());
        assertEquals(span, subject.wingSpan());
    }

    @Test
    void ensureAspectRatioIsCalculatedCorrectly() {
        // Exemplo: Span = 10, Area = 20 -> 10² / 20 = 100 / 20 = 5.0
        double area = 20.0;
        double span = 10.0;
        double expectedAspectRatio = 5.0;

        WingGeometry subject = WingGeometry.valueOf(area, span);
        assertEquals(expectedAspectRatio, subject.aspectRatio(), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 34",    // area zero
            "125, 0",   // span zero
            "-1, 34",   // area negativa
            "125, -1"   // span negativo
    })
    void ensureInvalidMeasurementsThrowException(double area, double span) {
        assertThrows(IllegalArgumentException.class, () ->
                WingGeometry.valueOf(area, span)
        );
    }

    @Test
    void testEqualsAndHashCode() {
        WingGeometry g1 = WingGeometry.valueOf(125, 34);
        WingGeometry g2 = WingGeometry.valueOf(125, 34);
        WingGeometry g3 = WingGeometry.valueOf(150, 34);

        // Identidade
        assertEquals(g1, g1);

        // Igualdade por valor
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        // Diferença
        assertNotEquals(g1, g3);
        assertNotEquals(g1, null);
        assertNotEquals("not a wing", g1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        WingGeometry subject = new WingGeometry() {};
        assertNotNull(subject);
    }
}
