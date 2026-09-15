package eapli.aisafe.aircontrolarea.domain.GeographicBoundary;

import eapli.aisafe.aircontrolarea.application.exceptions.NotEnoughPointException;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class GeographicBoundary {

    @ElementCollection
    private List<GeographicCoords> geoCords;

    private float geoArea;

    protected GeographicBoundary() {}

    public GeographicBoundary(List<GeographicCoords> geoCords){
        if(geoCords.size() < 3)
            throw new NotEnoughPointException(geoCords.size());
        this.geoCords = new ArrayList<>(geoCords);
    }

    public static GeographicBoundary valueOf(final List<GeographicCoords> geoCords) {
        return new GeographicBoundary(geoCords);
    }

    public List<GeographicCoords> getGeoCords(){ return geoCords; }

    /**
     * Determines whether a given point lies inside this geographic boundary.
     *
     * <p>Points coincident with a polygon vertex are considered inside.
     * Points on an edge (but not a vertex) are considered outside.</p>
     *
     * @param point the geographic point to test (latitude/longitude)
     * @return {@code true} if the point is inside this boundary
     */
    public boolean contains(final GeographicCoords point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null.");
        }

        final float x = point.getX();
        final float y = point.getY();

        for (final GeographicCoords vertex : geoCords) {
            if (vertex.equals(point)) {
                return true;
            }
        }

        final int n = geoCords.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if (isOnOpenEdge(x, y, geoCords.get(i), geoCords.get(j))) {
                return false;
            }
        }

        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            final GeographicCoords pi = geoCords.get(i);
            final GeographicCoords pj = geoCords.get(j);

            final boolean intersects = ((pi.getY() > y) != (pj.getY() > y))
                    && (x < (pj.getX() - pi.getX()) * (y - pi.getY()) / (pj.getY() - pi.getY()) + pi.getX());

            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static boolean isOnOpenEdge(final float x, final float y,
                                        final GeographicCoords a, final GeographicCoords b) {
        final float ax = a.getX();
        final float ay = a.getY();
        final float bx = b.getX();
        final float by = b.getY();

        final float cross = (y - ay) * (bx - ax) - (x - ax) * (by - ay);
        if (Math.abs(cross) > 1e-6f) {
            return false;
        }

        final float minX = Math.min(ax, bx);
        final float maxX = Math.max(ax, bx);
        final float minY = Math.min(ay, by);
        final float maxY = Math.max(ay, by);

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }

        final boolean atStart = Float.compare(x, ax) == 0 && Float.compare(y, ay) == 0;
        final boolean atEnd = Float.compare(x, bx) == 0 && Float.compare(y, by) == 0;
        return !atStart && !atEnd;
    }

}

/*
Gauss Area Formula used (might need to remember this name later)
 */


/*
TODO: REMEMBER GEO CORDS MIGHT NEED TO BE PROMOTED TO ENTITY ON DM

@author BKTales
 */
