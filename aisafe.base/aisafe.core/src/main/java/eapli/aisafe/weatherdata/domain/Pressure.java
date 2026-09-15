package eapli.aisafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class Pressure implements ValueObject {
    private double pressure;

    protected Pressure() {} // JPA

    public Pressure(double pressure){
        if(pressure < 0) throw new IllegalArgumentException("Pressure cannot be negative.");
        this.pressure = pressure;
    }

    public static Pressure valueOf(final double pressure) {
        return new Pressure(pressure);
    }

    public double getPressure() { return pressure; }
}
