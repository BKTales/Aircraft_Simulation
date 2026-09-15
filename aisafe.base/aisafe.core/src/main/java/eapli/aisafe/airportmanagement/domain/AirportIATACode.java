package eapli.aisafe.airportmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Represents the IATA code of an airport.
 *
 * <p>An airport IATA code consists of exactly three uppercase alphabetic characters,
 * as defined by the International Air Transport Association. Input is normalised
 * to uppercase regardless of the case supplied.</p>
 *
 * @author aisafe team
 */
@Embeddable
public class AirportIATACode implements ValueObject, Comparable<AirportIATACode> {

    @Column(name = "AIRPORT_IATA_CODE", nullable = false, unique = true, length = 3)
    private String code;

    protected AirportIATACode() {
        // for ORM
    }

    private AirportIATACode(final String code) {
        final String normalized = normalize(code);
        if(!normalized.matches("^[A-Z]{3}$")){
            throw new IllegalArgumentException("Airport IATA code must consist of exactly 3 letters.");
        }
        this.code = normalized;
    }

    public static AirportIATACode valueOf(final String code) {
        return new AirportIATACode(code);
    }

    private String normalize(final String value) {
        if(value == null) return "";
        return value.trim().toUpperCase();
    }

    @Override
    public int compareTo(final AirportIATACode other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final AirportIATACode that = (AirportIATACode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
