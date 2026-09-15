package eapli.aisafe.enginemodelmanagement.domain;


import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EngineModelId implements ValueObject, Comparable<EngineModelId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ENGINE_MODEL_ID")
    private String id;

    protected EngineModelId() {
        // for ORM
    }

    private EngineModelId(final String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be empty");
        }
        this.id = id;
    }

    public static EngineModelId valueOf(final String id){
        return new EngineModelId(id);
    }

    @Override
    public String toString() { return id; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EngineModelId modelId = (EngineModelId) o;
        return Objects.equals(id, modelId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public int compareTo(EngineModelId engineModelId) {
        return this.id.compareTo(engineModelId.id);
    }
}

