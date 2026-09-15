package eapli.aisafe.weatherdata.domain.winddata;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class WindDataSpeed implements ValueObject {
    private double speed;

    protected WindDataSpeed() {} // JPA

    public WindDataSpeed(double speed){
        if(speed < 0) throw new IllegalArgumentException("Speed cannot be negative.");
        this.speed = speed;
    }

    public static WindDataSpeed valueOf(final double speed) {
        return new WindDataSpeed(speed);
    }

    public double getSpeed() { return speed; }
}
