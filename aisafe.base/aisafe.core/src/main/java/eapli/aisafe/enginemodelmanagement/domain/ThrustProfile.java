package eapli.aisafe.enginemodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ThrustProfile implements ValueObject {

    private double thrustAtStatic; // kN
    private double thrustAtCruise; // kN

    protected ThrustProfile() {}

    private ThrustProfile(final double thrustAtStatic, final double thrustAtCruise) {
        if (thrustAtStatic <= 0 || thrustAtCruise <= 0) {
            throw new IllegalArgumentException("Thrust values must be positive.");
        }
        if (thrustAtStatic < thrustAtCruise) {
            throw new IllegalArgumentException("thrustAtStatic must be greater than or equal to thrustAtCruise.");
        }
        this.thrustAtStatic = thrustAtStatic;
        this.thrustAtCruise = thrustAtCruise;
    }

    public static ThrustProfile valueOf(final double thrustAtStatic, final double thrustAtCruise) {
        return new ThrustProfile(thrustAtStatic, thrustAtCruise);
    }


    public double thrustAtSpeed(double currentSpeed, double cruiseSpeed) {
        if (currentSpeed >= cruiseSpeed) return thrustAtCruise;
        return thrustAtStatic + (thrustAtCruise - thrustAtStatic) * (currentSpeed / cruiseSpeed);
    }

    public double thrustAtStatic() {
        return thrustAtStatic;
    }

    public double thrustAtCruise() {
        return thrustAtCruise;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrustProfile)) return false;
        ThrustProfile that = (ThrustProfile) o;
        return Double.compare(that.thrustAtStatic, thrustAtStatic) == 0 &&
                Double.compare(that.thrustAtCruise, thrustAtCruise) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(thrustAtStatic, thrustAtCruise);
    }
}