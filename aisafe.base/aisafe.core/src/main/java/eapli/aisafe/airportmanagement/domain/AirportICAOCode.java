package eapli.aisafe.airportmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Represents the ICAO code of an airport.
 *
 * <p>An airport ICAO code consists of exactly four uppercase alphanumeric characters,
 * as defined by the International Civil Aviation Organisation. Input is normalised
 * to uppercase regardless of the case supplied.</p>
 *
 * @author aisafe team
 */
@Embeddable
public class AirportICAOCode implements ValueObject, Comparable<AirportICAOCode> {

    @Column(name = "AIRPORT_ICAO_CODE", nullable = false, unique = true, length = 4)
    private String code;

    protected AirportICAOCode() {
        // for ORM
    }

    private AirportICAOCode(final String code) {
        final String normalized = normalize(code);
        if(!normalized.matches("^[A-Z0-9]{4}$")){
            throw new IllegalArgumentException("Airport ICAO code must consist of exactly 4 alphanumeric characters.");
        }
        this.code = normalized;
    }

    public static AirportICAOCode valueOf(final String code) {
        return new AirportICAOCode(code);
    }

    private String normalize(final String value) {
        if(value == null) return "";
        return value.trim().toUpperCase();
    }

    @Override
    public int compareTo(final AirportICAOCode other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final AirportICAOCode that = (AirportICAOCode) o;
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
