package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class WingGeometry implements ValueObject {

    private double wingArea; // m²
    private double wingSpan; // m

    protected WingGeometry() {

    }

    private WingGeometry(final double wingArea, final double wingSpan) {
        if (wingArea <= 0 || wingSpan <= 0) {
            throw new IllegalArgumentException("Wing measurements must be positive.");
        }
        this.wingArea = wingArea;
        this.wingSpan = wingSpan;
    }

    public static WingGeometry valueOf(final double wingArea, final double wingSpan){
        return new WingGeometry(wingArea,wingSpan);
    }

    public double wingArea() { return wingArea; }
    public double wingSpan() { return wingSpan; }

    /**
     * aspectRatio = wingSpan² / wingArea
     */
    public double aspectRatio() {
        return Math.pow(wingSpan, 2) / wingArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WingGeometry)) return false;
        WingGeometry that = (WingGeometry) o;
        return Double.compare(that.wingArea, wingArea) == 0 &&
                Double.compare(that.wingSpan, wingSpan) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wingArea, wingSpan);
    }
}