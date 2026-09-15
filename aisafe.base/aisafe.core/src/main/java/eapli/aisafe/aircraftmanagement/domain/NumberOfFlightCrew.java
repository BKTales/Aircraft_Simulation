package eapli.aisafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class NumberOfFlightCrew implements ValueObject {

    @Column(name = "NUMBER_OF_FLIGHT_CREW", nullable = false)
    private int count;

    protected NumberOfFlightCrew() {
        // ORM
    }

    private NumberOfFlightCrew(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Flight crew count must be positive.");
        }
        this.count = count;
    }

    public static NumberOfFlightCrew valueOf(final int count) {
        return new NumberOfFlightCrew(count);
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberOfFlightCrew)) {
            return false;
        }
        final NumberOfFlightCrew that = (NumberOfFlightCrew) o;
        return count == that.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count);
    }
}
