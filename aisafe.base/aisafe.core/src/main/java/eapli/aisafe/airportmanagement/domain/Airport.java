package eapli.aisafe.airportmanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;

/**
 * Represents an airport registered within an air control area.
 *
 * <p>An airport is uniquely identified by its IATA code and must carry a unique ICAO code,
 * geographic coordinates (including elevation), and a reference to the air control area
 * it belongs to. The air control area is referenced by its identity only (cross-aggregate
 * DDD reference — no JPA join).</p>
 *
 * @author aisafe team
 */
@Entity
@Table(name = "AIRPORT")
public class Airport implements AggregateRoot<AirportIATACode>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AirportIATACode iataCode;

    @Version
    private Long version;

    @Embedded
    private AirportICAOCode icaoCode;

    @Embedded
    private Coordinates coordinates;

    @Column(name = "AIR_CONTROL_AREA_CODE", nullable = false)
    private String airControlAreaCode;

    protected Airport() {
        // for ORM
    }

    /**
     * Creates a new {@code Airport}.
     *
     * @param iataCode           the unique IATA code of this airport
     * @param icaoCode           the unique ICAO code of this airport
     * @param coordinates        the geographic location and elevation of this airport
     * @param airControlArea     the air control area in which this airport is registered
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public Airport(final AirportIATACode iataCode, final AirportICAOCode icaoCode,
                   final Coordinates coordinates, final AreaCode airControlArea) {
        if(iataCode == null || icaoCode == null || coordinates == null || airControlArea == null){
            throw new IllegalArgumentException("IATA code, ICAO code, coordinates, and air control area are required.");
        }
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.coordinates = coordinates;
        this.airControlAreaCode = airControlArea.getCode();
    }

    @Override
    public AirportIATACode identity() {
        return iataCode;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    /**
     * Returns the ICAO code of this airport.
     *
     * @return the ICAO code
     */
    public AirportICAOCode icaoCode() {
        return icaoCode;
    }

    /**
     * Returns the geographic coordinates and elevation of this airport.
     *
     * @return the coordinates
     */
    public Coordinates coordinates() {
        return coordinates;
    }

    /**
     * Returns the code of the air control area in which this airport is registered.
     *
     * @return the air control area code string
     */
    public String airControlAreaCode() {
        return airControlAreaCode;
    }

    @Override
    public String toString() {
        return iataCode + " / " + icaoCode + " (" + airControlAreaCode + ")";
    }
}
