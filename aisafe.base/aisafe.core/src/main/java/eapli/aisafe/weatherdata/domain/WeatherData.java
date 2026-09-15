package eapli.aisafe.weatherdata.domain;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class WeatherData implements AggregateRoot<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Embedded
    private WeatherDate weatherDate;
    @Embedded
    private Humidity humidity;
    @Embedded
    private Pressure pressure;
    @Embedded
    private Temperature temperature;
    @Embedded
    private WindData windData;
    @Embedded
    @AssociationOverride(name = "geoBound.geoCords", joinTable = @JoinTable(name = "WEATHER_SECTION_COORDS"))
    private WeatherSection weatherSection;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_CONTROL_AREA_ID")
    private AirControlArea airControlArea;

    protected WeatherData() {} // JPA

    public WeatherData(WeatherDate weatherDate, Humidity humidity, Pressure pressure,
                       Temperature temperature, WindData windData, WeatherSection weatherSection,
                       AirControlArea airControlArea) {
        this.weatherDate = weatherDate;
        this.humidity = humidity;
        this.pressure = pressure;
        this.temperature = temperature;
        this.windData = windData;
        this.weatherSection = weatherSection;
        this.airControlArea = airControlArea;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public Long identity() { return id; }

    public WeatherDate getWeatherDate() { return weatherDate; }
    public Humidity getHumidity() { return humidity; }
    public Pressure getPressure() { return pressure; }
    public Temperature getTemperature() { return temperature; }
    public WindData getWindData() { return windData; }
    public WeatherSection getWeatherSection() { return weatherSection; }
    public AirControlArea getAirControlArea() { return airControlArea; }
}