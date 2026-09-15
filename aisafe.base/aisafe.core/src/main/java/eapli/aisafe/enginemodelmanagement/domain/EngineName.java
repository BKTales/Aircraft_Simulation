package eapli.aisafe.enginemodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EngineName implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    protected EngineName() {
        // for ORM
    }

    private EngineName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Engine name cannot be empty.");
        }
        this.name = name;
    }

    public static EngineName valueOf(final String name) {
        return new EngineName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineName)) return false;
        EngineName that = (EngineName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() { return name; }
}