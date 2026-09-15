package eapli.aisafe.weatherdata.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;

public class WeatherComplianceService {

    public boolean isSectionValidInsideArea(AirControlArea area, GeographicBoundary sectionBoundary) {
        GeographicBoundary areaBoundary = area.getGeographicBoundary();

        if (sectionBoundary.getGeoCords().isEmpty()) return false;

        for (GeographicCoords point : sectionBoundary.getGeoCords()) {
            if (!areaBoundary.contains(point)) {
                return false;
            }
        }
        return true;
    }
}
