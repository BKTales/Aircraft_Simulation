package eapli.aisafe.manufacturermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ManufacturerId implements ValueObject, Comparable<ManufacturerId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "MANUFACTURER_ID")
    private String code;

    protected ManufacturerId() {
        // for ORM
    }

    private ManufacturerId(final String code) {
        Preconditions.nonNull(code);
        Preconditions.nonEmpty(code);
        this.code = code.toUpperCase();
    }

    public static ManufacturerId valueOf(final String code){
        return new ManufacturerId(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManufacturerId that)) return false;
        return Objects.equals(code, that.code.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public int compareTo(ManufacturerId o) {
        return this.code.compareTo(o.code.toUpperCase());
    }
}