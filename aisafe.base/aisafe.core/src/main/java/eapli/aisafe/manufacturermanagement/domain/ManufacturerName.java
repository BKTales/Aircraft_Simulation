package eapli.aisafe.manufacturermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ManufacturerName implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "MANUFACTURER_NAME")
    private String name;

    protected ManufacturerName() {
        // for ORM
    }

    public ManufacturerName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Manufacturer Name cannot be empty.");
        }
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManufacturerName)) return false;
        ManufacturerName that = (ManufacturerName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

}