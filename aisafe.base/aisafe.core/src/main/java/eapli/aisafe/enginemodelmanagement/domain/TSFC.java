package eapli.aisafe.enginemodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TSFC implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "TSFC_VALUE")
    private double value;

    protected TSFC() {
        // for ORM
    }

    private TSFC(final double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("TSFC must be a positive value.");
        }
        this.value = value;
    }

    public static TSFC valueOf(final double value) {
        return new TSFC(value);
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TSFC)) return false;
        TSFC tsfc = (TSFC) o;
        return Double.compare(tsfc.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}