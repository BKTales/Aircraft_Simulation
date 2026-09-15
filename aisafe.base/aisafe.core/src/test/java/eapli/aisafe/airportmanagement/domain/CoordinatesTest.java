package eapli.aisafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoordinatesTest {

    @Test
    void ensureValidCoordinatesAreAccepted() {
        final Coordinates coords = Coordinates.valueOf(38.77, -9.13, 113.0);
        assertEquals(38.77, coords.latitude());
        assertEquals(-9.13, coords.longitude());
        assertEquals(113.0, coords.elevationMeters());
    }

    @Test
    void ensureZeroElevationIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(0.0, 0.0, 0.0);
        assertEquals(0.0, coords.elevationMeters());
    }

    @Test
    void ensureMaxLatitudeIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(90.0, 0.0, 0.0);
        assertEquals(90.0, coords.latitude());
    }

    @Test
    void ensureMinLatitudeIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(-90.0, 0.0, 0.0);
        assertEquals(-90.0, coords.latitude());
    }

    @Test
    void ensureMaxLongitudeIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(0.0, 180.0, 0.0);
        assertEquals(180.0, coords.longitude());
    }

    @Test
    void ensureMinLongitudeIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(0.0, -180.0, 0.0);
        assertEquals(-180.0, coords.longitude());
    }

    @Test
    void ensureLargeElevationIsAccepted() {
        final Coordinates coords = Coordinates.valueOf(27.98, 86.92, 5364.0);
        assertEquals(5364.0, coords.elevationMeters());
    }

    @Test
    void ensureLatitudeAbove90Throws() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                Coordinates.valueOf(90.1, 0.0, 0.0);
            }
        });
    }

    @Test
    void ensureLatitudeBelow90Throws() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                Coordinates.valueOf(-90.1, 0.0, 0.0);
            }
        });
    }

    @Test
    void ensureLongitudeAbove180Throws() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                Coordinates.valueOf(0.0, 180.1, 0.0);
            }
        });
    }

    @Test
    void ensureLongitudeBelow180Throws() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                Coordinates.valueOf(0.0, -180.1, 0.0);
            }
        });
    }

    @Test
    void ensureNegativeElevationThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                Coordinates.valueOf(0.0, 0.0, -1.0);
            }
        });
    }

    @Test
    void ensureEqualCoordinatesAreEqual() {
        final Coordinates a = Coordinates.valueOf(38.77, -9.13, 113.0);
        final Coordinates b = Coordinates.valueOf(38.77, -9.13, 113.0);
        assertEquals(a, b);
    }

    @Test
    void ensureDifferentCoordinatesAreNotEqual() {
        final Coordinates a = Coordinates.valueOf(38.77, -9.13, 113.0);
        final Coordinates b = Coordinates.valueOf(37.02, -7.97, 8.0);
        assertNotEquals(a, b);
    }

    @Test
    void ensureEqualCoordinatesHaveSameHashCode() {
        final Coordinates a = Coordinates.valueOf(38.77, -9.13, 113.0);
        final Coordinates b = Coordinates.valueOf(38.77, -9.13, 113.0);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureToStringContainsAllComponents() {
        final Coordinates coords = Coordinates.valueOf(38.77, -9.13, 113.0);
        final String s = coords.toString();
        assertEquals("(38.77, -9.13, 113.0m)", s);
    }
}
