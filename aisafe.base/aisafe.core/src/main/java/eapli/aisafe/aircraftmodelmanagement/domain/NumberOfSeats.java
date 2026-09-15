package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class NumberOfSeats implements ValueObject {

    @Column(name = "NUMBER_OF_SEATS")
    private int seats;

    protected NumberOfSeats() {
    }

    private NumberOfSeats(final int seats) {
        if (seats < 0) {
            throw new IllegalArgumentException("Number of seats cannot be negative.");
        }
        this.seats = seats;
    }

    public int seats() {
        return seats;
    }

    public static NumberOfSeats valueOf(final int seats) {
        return new NumberOfSeats(seats);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberOfSeats)) {
            return false;
        }
        final NumberOfSeats that = (NumberOfSeats) o;
        return seats == that.seats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seats);
    }

    @Override
    public String toString() {
        return Integer.toString(seats);
    }
}
