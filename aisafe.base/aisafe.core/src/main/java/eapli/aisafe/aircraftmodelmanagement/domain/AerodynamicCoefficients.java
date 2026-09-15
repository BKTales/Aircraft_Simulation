package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class AerodynamicCoefficients implements ValueObject {

    private double cd0; // Zero-lift drag coefficient
    private double cl;  // Lift coefficient


    protected AerodynamicCoefficients() {

    }

    private AerodynamicCoefficients(final double cd0, final double cl) {
        if (cd0 < 0) {
            throw new IllegalArgumentException("cd0 must be non-negative");
        }
        this.cd0 = cd0;
        this.cl = cl;
    }

    public static AerodynamicCoefficients valueOf(final double cd0, final double cl){
        return new AerodynamicCoefficients(cd0,cl);
    }


    public double cd0() { return cd0; }
    public double cl() { return cl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AerodynamicCoefficients)) return false;
        AerodynamicCoefficients that = (AerodynamicCoefficients) o;
        return Double.compare(that.cd0, cd0) == 0 &&
                Double.compare(that.cl, cl) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cd0, cl);
    }
}