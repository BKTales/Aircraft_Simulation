package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class FuelLoad implements ValueObject {

    private double quantityKg;

    protected FuelLoad() {
        // ORM
    }

    public FuelLoad(final double quantityKg) {
        if (quantityKg < 0) {
            throw new IllegalArgumentException("Fuel load cannot be negative.");
        }
        this.quantityKg = quantityKg;
    }

    public double quantityKg() {
        return quantityKg;
    }

    public static FuelLoad valueOf(final double quantityKg) {
        return new FuelLoad(quantityKg);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FuelLoad fuelLoad = (FuelLoad) o;
        return Double.compare(fuelLoad.quantityKg, quantityKg) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantityKg);
    }
}
