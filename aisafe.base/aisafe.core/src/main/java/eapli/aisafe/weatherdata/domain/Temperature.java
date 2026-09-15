package eapli.aisafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class Temperature implements ValueObject {

    private double temperature;

    protected Temperature() {} // JPA

    public Temperature(double temperature){
        this.temperature = temperature;
    }

    public static Temperature valueOf(final double temperature) {
        return new Temperature(temperature);
    }

    public double getTemperature() { return temperature; }
}
