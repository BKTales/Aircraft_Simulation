package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.exceptions.NotEnoughPointException;
import eapli.aisafe.aircontrolarea.application.exceptions.ZeroSizeAreaException;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundaryService;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeographicBoundaryTest {

    private final GeographicBoundaryService service = new GeographicBoundaryService();

    @Test
    public void testNotEnoughPointsException() {
        assertThrows(NotEnoughPointException.class, () ->
                GeographicBoundary.valueOf(Arrays.asList(GeographicCoords.valueOf(0,0), GeographicCoords.valueOf(1,1)))
        );
    }

    @Test
    public void testZeroSizeAreaException() {
        // Pontos colineares resultam em área 0
        GeographicBoundary line = GeographicBoundary.valueOf(Arrays.asList(
                GeographicCoords.valueOf(0,0), GeographicCoords.valueOf(1,1), GeographicCoords.valueOf(2,2)
        ));
        assertThrows(ZeroSizeAreaException.class, () -> service.calculateArea(line));
    }

    @Test
    public void testGeographicCoordsTechnicalMethods() {
        GeographicCoords c1 = GeographicCoords.valueOf(10, 20);
        GeographicCoords c2 = GeographicCoords.valueOf(10, 20);
        GeographicCoords c3 = GeographicCoords.valueOf(30, 40);

        assertEquals(c1, c1);
        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertNotEquals(c1, null);
        assertNotEquals(c1, "not a coord");

        class ProtectedCoords extends GeographicCoords {
            public ProtectedCoords() { super(); }
        }
        assertNotNull(new ProtectedCoords());
    }

    @Test
    public void testCalculateAreaAbsoluteZero() {
        GeographicBoundaryService service = new GeographicBoundaryService();
        GeographicBoundary point = GeographicBoundary.valueOf(Arrays.asList(
                GeographicCoords.valueOf(1,1), GeographicCoords.valueOf(1,1), GeographicCoords.valueOf(1,1)
        ));

        assertThrows(ZeroSizeAreaException.class, () -> service.calculateArea(point));
    }

    @Test
    public void testComplexCollisionBranches() {
        GeographicBoundaryService service = new GeographicBoundaryService();

        GeographicBoundary square = createSquare(0, 10);
        GeographicBoundary triangle = GeographicBoundary.valueOf(Arrays.asList(
                GeographicCoords.valueOf(5,5), GeographicCoords.valueOf(15,5), GeographicCoords.valueOf(5,15)
        ));

        assertTrue(service.checkCollision(square, triangle));
    }

    @Test
    public void testCollisionDetectionAndException() {
        GeographicBoundary b1 = createSquare(0, 10);
        GeographicBoundary b2 = createSquare(5, 15);

        assertTrue(service.checkCollision(b1, b2));
    }

    @Test
    public void testNoCollision() {
        GeographicBoundary b1 = createSquare(0, 5);
        GeographicBoundary b2 = createSquare(10, 15); // Longe de b1
        assertFalse(service.checkCollision(b1, b2));
    }

    @Test
    public void testContainsPointInsideAndOutside() {
        GeographicBoundary square = createSquare(0, 10);
        assertTrue(square.contains(GeographicCoords.valueOf(5, 5)));
        assertFalse(square.contains(GeographicCoords.valueOf(20, 20)));
    }

    @Test
    public void testContainsWithHorizontalAndBoundaryCases() {
        GeographicBoundary square = createSquare(0, 10);
        assertFalse(square.contains(GeographicCoords.valueOf(-1, 5)));
        assertFalse(square.contains(GeographicCoords.valueOf(15, 5)));
    }

    @Test
    public void testContainsPointOnVertexIsInside() {
        GeographicBoundary square = createSquare(0, 10);
        assertTrue(square.contains(GeographicCoords.valueOf(0, 0)));
        assertTrue(square.contains(GeographicCoords.valueOf(10, 10)));
    }

    @Test
    public void testContainsPointOnEdgeIsOutside() {
        GeographicBoundary square = createSquare(0, 10);
        assertFalse(square.contains(GeographicCoords.valueOf(5, 0)));
        assertFalse(square.contains(GeographicCoords.valueOf(0, 5)));
    }

    @Test
    public void testContainsNullPointThrows() {
        GeographicBoundary square = createSquare(0, 10);
        assertThrows(IllegalArgumentException.class, () -> square.contains(null));
    }

    @Test
    public void testGetGeoCords() {
        GeographicBoundary square = createSquare(0, 10);
        assertEquals(4, square.getGeoCords().size());
    }

    @Test
    public void testPositiveAreaAndProtectedConstructorCoverage() {
        GeographicBoundary triangle = GeographicBoundary.valueOf(Arrays.asList(
                GeographicCoords.valueOf(0, 0), GeographicCoords.valueOf(4, 0), GeographicCoords.valueOf(0, 3)
        ));

        assertTrue(service.calculateArea(triangle) > 0);

        class ProtectedBoundary extends GeographicBoundary {
            public ProtectedBoundary() { super(); }
        }
        assertNotNull(new ProtectedBoundary());
    }

    @Test
    public void testCollisionWithEmptyPolygonsTriggersOverlapPath() {
        GeographicBoundary emptyA = mock(GeographicBoundary.class);
        GeographicBoundary emptyB = mock(GeographicBoundary.class);
        when(emptyA.getGeoCords()).thenReturn(new ArrayList<>());
        when(emptyB.getGeoCords()).thenReturn(new ArrayList<>());

        assertTrue(service.checkCollision(emptyA, emptyB));
    }

    private GeographicBoundary createSquare(float start, float end) {
        return GeographicBoundary.valueOf(Arrays.asList(
                GeographicCoords.valueOf(start, start), GeographicCoords.valueOf(end, start),
                GeographicCoords.valueOf(end, end), GeographicCoords.valueOf(start, end)
        ));
    }
}
