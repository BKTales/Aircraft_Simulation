package eapli.aisafe.aircontrolarea.domain;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundaryService;
import eapli.aisafe.weatherdata.application.WeatherComplianceService;
import eapli.aisafe.weatherdata.application.exceptions.WeatherSectionOutOfBoundsException;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AIR_CONTROL_AREA")
public class AirControlArea implements AggregateRoot<AreaCode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Embedded
    private AirControlAreaName name;

    @Embedded
    @AssociationOverride(name = "geoCords", joinTable = @JoinTable(name = "AIR_CONTROL_AREA_COORDS"))
    private GeographicBoundary geographicBoundary;

    @Embedded
    private AreaCode areaCode;

    @Embedded
    private MinFuelRequirement minFuelRequirement;

    @ElementCollection
    private List<WeatherData> weatherSections = new ArrayList<>();

    protected AirControlArea() {}

    public AirControlArea(AirControlAreaName name, GeographicBoundary geoBound, MinFuelRequirement minFuelRequirement){
        this.name = name;
        this.geographicBoundary = geoBound;
        this.areaCode = new AreaCode();
        this.minFuelRequirement = minFuelRequirement;
    }

    public void addWeatherSection(WeatherData data, WeatherComplianceService validator) {
        if (!validator.isSectionValidInsideArea(this, data.getWeatherSection().getGeoBound())) {
            throw new WeatherSectionOutOfBoundsException();
        }
        this.weatherSections.add(data);
    }

    public boolean sameAs(Object other) {
        if (!(other instanceof AirControlArea)) return false;
        final AirControlArea that = (AirControlArea) other;
        return this.identity().equals(that.identity());
    }

    public AreaCode identity() {
        return this.areaCode;
    }

    public AirControlAreaName getName() { return name; }
    public GeographicBoundary getGeographicBoundary() { return geographicBoundary; }
    public AreaCode getAreaCode() { return areaCode; }
    public MinFuelRequirement getMinFuelRequirement() { return minFuelRequirement; }
}