package eapli.aisafe.weatherdata.repositories;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryWeatherDataRepository
        extends InMemoryDomainRepository<WeatherData, Long>
        implements WeatherDataRepository {

    @Override
    public List<WeatherData> findByAreaAndInterval(final AreaCode areaCode, final LocalDateTime start,
                                                   final LocalDateTime end) {
        final List<WeatherData> result = new ArrayList<>();
        for (final WeatherData weatherData : findAll()) {
            if (weatherData.getAirControlArea() == null || weatherData.getAirControlArea().identity() == null) {
                continue;
            }
            if (!weatherData.getAirControlArea().identity().equals(areaCode)) {
                continue;
            }
            if (weatherData.getWeatherDate().getStartDateTime().isAfter(end)
                    || weatherData.getWeatherDate().getEndDateTime().isBefore(start)) {
                continue;
            }
            result.add(weatherData);
        }
        return result;
    }
}