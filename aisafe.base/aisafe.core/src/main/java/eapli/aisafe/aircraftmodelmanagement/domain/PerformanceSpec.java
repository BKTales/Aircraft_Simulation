package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class PerformanceSpec implements ValueObject {

    private double serviceCeiling;
    private double cruiseSpeed;
    private double fuelCapacity;
    private double maxRange;

    protected PerformanceSpec() {

    }

    private PerformanceSpec(final double serviceCeiling,
                           final double cruiseSpeed,
                           final double fuelCapacity,
                           final double maxRange) {
        if (serviceCeiling <= 0 || cruiseSpeed <= 0 || fuelCapacity <= 0 || maxRange <= 0) {
            throw new IllegalArgumentException("Performance values must be positive.");
        }
        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
    }


    public static PerformanceSpec valueOf(final double serviceCeiling,
                                          final double cruiseSpeed,
                                          final double fuelCapacity,
                                          final double maxRange){
        return new PerformanceSpec(serviceCeiling,cruiseSpeed,fuelCapacity,maxRange);
    }


    public double serviceCeiling() {
        return serviceCeiling;
    }

    public double cruiseSpeed() {
        return cruiseSpeed;
    }

    public double fuelCapacity() {
        return fuelCapacity;
    }

    public double maxRange() {
        return maxRange;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PerformanceSpec)) return false;
        final PerformanceSpec that = (PerformanceSpec) o;
        return Double.compare(that.serviceCeiling, serviceCeiling) == 0
                && Double.compare(that.cruiseSpeed, cruiseSpeed) == 0
                && Double.compare(that.fuelCapacity, fuelCapacity) == 0
                && Double.compare(that.maxRange, maxRange) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceCeiling, cruiseSpeed, fuelCapacity, maxRange);
    }
}
