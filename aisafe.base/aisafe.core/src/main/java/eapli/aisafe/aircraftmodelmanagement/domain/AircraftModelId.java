package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AircraftModelId implements ValueObject, Comparable<AircraftModelId> {
    private String code;

    protected AircraftModelId(){

    }

    private AircraftModelId(final String code) {
        Preconditions.nonNull(code,"Model Id cannot be null");
        Preconditions.nonEmpty(code,"Model Id cannot be empty");
        this.code = code;
    }

    public static AircraftModelId valueOf(final String code){
        return new AircraftModelId(code);
    }


    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AircraftModelId modelId = (AircraftModelId) o;
        return Objects.equals(code, modelId.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public int compareTo(AircraftModelId aircraftModelId) {
        if (aircraftModelId == null) {
            throw new IllegalArgumentException("Aircraft model id is required.");
        }
        return this.code.compareTo(aircraftModelId.code);
    }
}
