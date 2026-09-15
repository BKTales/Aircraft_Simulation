package eapli.aisafe.weatherdata.domain.winddata;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class WindData implements ValueObject {
    @Embedded
    private WindDataDirection direction;
    @Embedded
    private WindDataSpeed speed;

    protected WindData() {} // JPA

    public WindData(WindDataDirection direction, WindDataSpeed speed){
        this.direction = direction;
        this.speed = speed;
    }

    public static WindData valueOf(final WindDataDirection direction, final WindDataSpeed speed) {
        return new WindData(direction, speed);
    }

    public WindDataSpeed getSpeed() { return speed; }
    public WindDataDirection getDirection() { return direction; }
}
