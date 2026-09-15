package eapli.aisafe.weatherdata.domain.winddata;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class WindDataDirection implements ValueObject {
    private static final int CIRCLE_RADIUS_DEGREES = 360;
    private int windDirection;

    protected WindDataDirection() {} // JPA

    public WindDataDirection(int windDirection){
        int circleRadiusCount = 0;
        if(windDirection > CIRCLE_RADIUS_DEGREES){
            circleRadiusCount = Math.floorDiv(windDirection, CIRCLE_RADIUS_DEGREES);
        }
        this.windDirection = Math.abs(windDirection - circleRadiusCount * CIRCLE_RADIUS_DEGREES);
    }

    public static WindDataDirection valueOf(final int windDirection) {
        return new WindDataDirection(windDirection);
    }

    public int getWindDirection() { return windDirection; }
}
