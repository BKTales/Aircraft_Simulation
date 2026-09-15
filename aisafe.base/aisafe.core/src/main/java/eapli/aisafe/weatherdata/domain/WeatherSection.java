package eapli.aisafe.weatherdata.domain;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class WeatherSection implements ValueObject {
    @Embedded
    private GeographicBoundary geoBound;

    protected WeatherSection() {} // JPA

    public WeatherSection(GeographicBoundary geoBound){
        if (geoBound == null) throw new IllegalArgumentException("Boundary cannot be null.");
        this.geoBound = geoBound;
    }

    public static WeatherSection valueOf(final GeographicBoundary geoBound) {
        return new WeatherSection(geoBound);
    }

    public GeographicBoundary getGeoBound() { return geoBound; }
}
