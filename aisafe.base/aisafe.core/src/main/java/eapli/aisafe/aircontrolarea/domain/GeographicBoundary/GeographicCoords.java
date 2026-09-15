package eapli.aisafe.aircontrolarea.domain.GeographicBoundary;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

/**
 * A 2D geographic position used as a polygon vertex or point-in-polygon query.
 *
 * <p>{@code x} is latitude (decimal degrees, [-90, 90]) and {@code y} is longitude
 * (decimal degrees, [-180, 180]).</p>
 */
@Embeddable
public class GeographicCoords implements ValueObject {

    private float x;
    private float y;

    protected GeographicCoords() {
    }

    public GeographicCoords(final float x, final float y) {
        validate(x, y);
        this.x = x;
        this.y = y;
    }

    public static GeographicCoords valueOf(final float latitude, final float longitude) {
        return new GeographicCoords(latitude, longitude);
    }

    public static GeographicCoords valueOf(final double latitude, final double longitude) {
        return new GeographicCoords((float) latitude, (float) longitude);
    }

    /** @return latitude in decimal degrees */
    public float getX() {
        return x;
    }

    /** @return longitude in decimal degrees */
    public float getY() {
        return y;
    }

    private static void validate(final float latitude, final float longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographicCoords that = (GeographicCoords) o;
        return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}
