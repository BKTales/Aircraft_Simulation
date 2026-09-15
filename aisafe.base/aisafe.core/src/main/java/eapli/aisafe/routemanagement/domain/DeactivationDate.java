package eapli.aisafe.routemanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class DeactivationDate implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "DEACTIVATION_DATE")
    private LocalDate value;

    protected DeactivationDate() {
        // for ORM
    }

    public DeactivationDate(final LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("Deactivation date is required.");
        }
        this.value = value;
    }

    public static DeactivationDate valueOf(final LocalDate value) {
        return new DeactivationDate(value);
    }

    public LocalDate value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeactivationDate that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
