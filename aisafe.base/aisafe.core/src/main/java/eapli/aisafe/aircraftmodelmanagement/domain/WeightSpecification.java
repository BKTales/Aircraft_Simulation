package eapli.aisafe.aircraftmodelmanagement.domain;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class WeightSpecification implements ValueObject {

    private double mtow; // Maximum Take-Off Weight
    private double mzfw; // Maximum Zero Fuel Weight
    private double emptyWeight;


    protected WeightSpecification() {

    }

    private WeightSpecification(final double mtow, final double mzfw, final double emptyWeight) {
        if (mtow <= 0 || mzfw <= 0 || emptyWeight <= 0) {
            throw new IllegalArgumentException("Weights must be positive values.");
        }
        if (mtow <= mzfw || mzfw <= emptyWeight) {
            throw new IllegalArgumentException("Weights must satisfy MTOW > MZFW > empty weight.");
        }
        this.mtow = mtow;
        this.mzfw = mzfw;
        this.emptyWeight = emptyWeight;
    }

    public static WeightSpecification valueOf(final double mtow, final double mzfw, final double emptyWeight){
        return new WeightSpecification(mtow,mzfw,emptyWeight);
    }


    public double mtow() { return mtow; }
    public double mzfw() { return mzfw; }
    public double emptyWeight() { return emptyWeight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeightSpecification)) return false;
        WeightSpecification that = (WeightSpecification) o;
        return Double.compare(that.mtow, mtow) == 0 &&
                Double.compare(that.mzfw, mzfw) == 0 &&
                Double.compare(that.emptyWeight, emptyWeight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mtow, mzfw, emptyWeight);
    }
}
