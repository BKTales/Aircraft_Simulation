package eapli.aisafe.airportmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Represents the geographic location of an airport, including latitude, longitude,
 * and elevation above sea level.
 *
 * <p>Latitude must be within the range [-90, 90] and longitude within [-180, 180].
 * Elevation must be zero or greater, as airports must be at or above sea level.</p>
 *
 * @author aisafe team
 */
@Embeddable
public class Coordinates implements ValueObject {

    @Column(name = "COORD_LATITUDE", nullable = false)
    private double latitude;

    @Column(name = "COORD_LONGITUDE", nullable = false)
    private double longitude;

    @Column(name = "COORD_ELEVATION_METERS", nullable = false)
    private double elevationMeters;

    protected Coordinates() {
        // for ORM
    }

    private Coordinates(final double latitude, final double longitude, final double elevationMeters) {
        if(latitude < -90 || latitude > 90){
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if(longitude < -180 || longitude > 180){
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
        if(elevationMeters < 0){
            throw new IllegalArgumentException("Elevation must be zero or greater (above sea level).");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevationMeters = elevationMeters;
    }

    /**
     * @param latitude        decimal degrees [-90, 90]
     * @param longitude       decimal degrees [-180, 180]
     * @param elevationMeters metres above sea level, {@code >= 0}
     */
    public static Coordinates valueOf(final double latitude, final double longitude, final double elevationMeters) {
        return new Coordinates(latitude, longitude, elevationMeters);
    }

    /**
     * Returns the latitude in decimal degrees.
     *
     * @return latitude
     */
    public double latitude() {
        return latitude;
    }

    /**
     * Returns the longitude in decimal degrees.
     *
     * @return longitude
     */
    public double longitude() {
        return longitude;
    }

    /**
     * Returns the elevation in metres above sea level.
     *
     * @return elevation in metres
     */
    public double elevationMeters() {
        return elevationMeters;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.elevationMeters, elevationMeters) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, elevationMeters);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ", " + elevationMeters + "m)";
    }
}
