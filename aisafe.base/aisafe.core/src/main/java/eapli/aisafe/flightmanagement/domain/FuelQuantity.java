package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;

import java.util.Locale;
import java.util.Objects;

/**
 * Fuel amount with unit for flight-plan composition (US080 / DSL).
 */
public final class FuelQuantity implements ValueObject {

    private static final double LITRES_TO_KG = 0.804;

    private final double amount;
    private final String unit;

    public FuelQuantity(final double amount, final String unit) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Fuel amount must be greater than zero.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Fuel unit is required.");
        }
        final String normalized = unit.trim().toLowerCase(Locale.ROOT);
        if (!"kg".equals(normalized) && !"l".equals(normalized)) {
            throw new IllegalArgumentException("Fuel unit must be kg or l.");
        }
        this.amount = amount;
        this.unit = normalized;
    }

    public static FuelQuantity kilograms(final double amount) {
        return new FuelQuantity(amount, "kg");
    }

    public double amount() {
        return amount;
    }

    public String unit() {
        return unit;
    }

    public double toKg() {
        if ("l".equals(unit)) {
            return amount * LITRES_TO_KG;
        }
        return amount;
    }

    public FuelLoad toFuelLoad() {
        return new FuelLoad(toKg());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FuelQuantity that)) {
            return false;
        }
        return Double.compare(that.amount, amount) == 0 && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, unit);
    }
}
