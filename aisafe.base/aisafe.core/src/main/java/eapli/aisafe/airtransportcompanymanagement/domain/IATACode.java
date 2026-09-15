package eapli.aisafe.airtransportcompanymanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class IATACode implements ValueObject, Comparable<IATACode> {

    @Column(name = "IATA_CODE")
    private String code;

    protected IATACode() {
        // for ORM
    }

    private IATACode(final String code) {
        final String normalized = normalize(code);
        if (!normalized.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("IATA code must consist of exactly 2 letters.");
        }
        this.code = normalized;
    }

    public static IATACode valueOf(final String code) {
        return new IATACode(code);
    }

    private String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    @Override
    public int compareTo(final IATACode o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IATACode iataCode = (IATACode) o;
        return Objects.equals(code, iataCode.code);
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
