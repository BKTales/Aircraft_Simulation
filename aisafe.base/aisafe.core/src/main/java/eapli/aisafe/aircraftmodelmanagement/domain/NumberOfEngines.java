package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class NumberOfEngines implements ValueObject {

    private int number;

    protected NumberOfEngines() {

    }

    private NumberOfEngines(final int number) {
        Preconditions.isPositive(number,"Number of engines must be positive.");
        this.number = number;
    }

    public static NumberOfEngines valueOf(final int number){
        return new NumberOfEngines(number);
    }

    public int number() {
        return number;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberOfEngines)) return false;
        final NumberOfEngines that = (NumberOfEngines) o;
        return number == that.number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
