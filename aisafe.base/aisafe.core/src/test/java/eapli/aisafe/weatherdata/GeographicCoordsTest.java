package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeographicCoordsTest {

    @Test
    void testCoordsConstruction() {
        GeographicCoords coords = GeographicCoords.valueOf(10.5f, 20.5f);
        assertEquals(10.5f, coords.getX());
        assertEquals(20.5f, coords.getY());
    }

    @Test
    void testCoordsConstructionFromDouble() {
        GeographicCoords coords = GeographicCoords.valueOf(38.77, -9.13);
        assertEquals(38.77f, coords.getX(), 0.0001f);
        assertEquals(-9.13f, coords.getY(), 0.0001f);
    }

    @Test
    void testEqualsAndHashCode() {
        GeographicCoords c1 = GeographicCoords.valueOf(10, 10);
        GeographicCoords c2 = GeographicCoords.valueOf(10, 10);
        GeographicCoords c3 = GeographicCoords.valueOf(20, 20);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void ensureValidBoundaryLimitsAreAccepted() {
        assertNotNull(GeographicCoords.valueOf(90f, 180f));
        assertNotNull(GeographicCoords.valueOf(-90f, -180f));
    }

    @Test
    void ensureLatitudeAboveMaxIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeographicCoords.valueOf(91f, 0f));
    }

    @Test
    void ensureLatitudeBelowMinIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeographicCoords.valueOf(-91f, 0f));
    }

    @Test
    void ensureLongitudeAboveMaxIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeographicCoords.valueOf(0f, 181f));
    }

    @Test
    void ensureLongitudeBelowMinIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeographicCoords.valueOf(0f, -181f));
    }
}
