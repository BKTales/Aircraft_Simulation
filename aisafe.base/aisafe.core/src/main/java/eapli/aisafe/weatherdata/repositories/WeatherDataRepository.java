package eapli.aisafe.weatherdata.repositories;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherDataRepository extends DomainRepository<Long, WeatherData> {
    /**
     * Returns weather sections for an area whose interval intersects the
     * provided [start, end] window.
     */
    List<WeatherData> findByAreaAndInterval(AreaCode areaCode, LocalDateTime start, LocalDateTime end);
}
