package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class FlightLoad implements ValueObject {

    private int passengerCount;
    private double passengerWeight;
    private double cargoWeight;

    protected FlightLoad() {
        // ORM
    }

    public FlightLoad(final int passengerCount, final double passengerWeight, final double cargoWeight) {
        if (passengerCount < 0 || passengerWeight < 0 || cargoWeight < 0) {
            throw new IllegalArgumentException("Load values cannot be negative.");
        }
        this.passengerCount = passengerCount;
        this.passengerWeight = passengerWeight;
        this.cargoWeight = cargoWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightLoad that = (FlightLoad) o;
        return passengerCount == that.passengerCount &&
                Double.compare(that.passengerWeight, passengerWeight) == 0 &&
                Double.compare(that.cargoWeight, cargoWeight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerCount, passengerWeight, cargoWeight);
    }

    public int passengerCount() { return passengerCount; }
    public double passengerWeight() { return passengerWeight; }
    public double cargoWeight() { return cargoWeight; }

    public double totalPayloadMass() {
        return this.passengerWeight + this.cargoWeight;
    }
}