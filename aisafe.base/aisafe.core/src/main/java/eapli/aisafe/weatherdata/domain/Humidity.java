package eapli.aisafe.weatherdata.domain;

import eapli.aisafe.weatherdata.application.exceptions.InvalidHumidityValueException;
import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class Humidity implements ValueObject {
    private double humidity;

    protected Humidity() {} // JPA

    public Humidity(double humidity){
        if(humidity > 100 || humidity < 0) {
            throw new InvalidHumidityValueException();
        }
        this.humidity = humidity;
    }

    public static Humidity valueOf(final double humidity) {
        return new Humidity(humidity);
    }

    public double getHumidity() { return humidity; }
}
