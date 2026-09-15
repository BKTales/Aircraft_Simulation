package eapli.aisafe.airtransportcompanymanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ICAOCode implements ValueObject, Comparable<ICAOCode> {

    @Column(name = "ICAO_CODE")
    private String code;

    protected ICAOCode() {
        // for ORM
    }

    private ICAOCode(final String code) {
        final String normalized = normalize(code);
        if (!normalized.matches("^[A-Z]{2,3}$")) {
            throw new IllegalArgumentException("ICAO code must consist of 2 to 3 letters.");
        }
        this.code = normalized;
    }

    public static ICAOCode valueOf(final String code) {
        return new ICAOCode(code);
    }

    private String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    @Override
    public int compareTo(final ICAOCode o) {
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
        final ICAOCode icaoCode = (ICAOCode) o;
        return Objects.equals(code, icaoCode.code);
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
