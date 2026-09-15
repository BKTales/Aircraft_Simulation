package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ModelName implements ValueObject {

    @Column(name = "MODEL_NAME")
    private String name;


    protected ModelName() {

    }

    private ModelName(final String name) {
        Preconditions.nonNull(name);
        Preconditions.nonEmpty(name);
        this.name = name;
    }



    public String name() {
        return name;
    }

    public static ModelName valueOf(final String name) {
        return new ModelName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelName)) return false;
        ModelName that = (ModelName) o;
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