package eapli.aisafe.rcomp.server.weather;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.weatherdata.domain.Humidity;
import eapli.aisafe.weatherdata.domain.Pressure;
import eapli.aisafe.weatherdata.domain.Temperature;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.domain.WeatherDate;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherResponseFormatterTest {

    @Test
    void formatAreaIncludesBoundaryCoordinates() {
        final AirControlArea area = mock(AirControlArea.class);
        final GeographicBoundary boundary = mock(GeographicBoundary.class);
        when(area.identity()).thenReturn(AreaCode.valueOf("AREA-0"));
        when(area.getGeographicBoundary()).thenReturn(boundary);
        when(boundary.getGeoCords()).thenReturn(List.of(
                new GeographicCoords(1f, 2f),
                new GeographicCoords(3f, 4f)));

        final String line = WeatherResponseFormatter.formatArea(area);

        assertEquals("AREA-0|1.0:2.0,3.0:4.0", line);
    }

    @Test
    void formatWeatherAndJoinLines() {
        final WeatherData weather = mock(WeatherData.class);
        final AirControlArea area = mock(AirControlArea.class);
        when(weather.identity()).thenReturn(7L);
        when(weather.getAirControlArea()).thenReturn(area);
        when(area.identity()).thenReturn(AreaCode.valueOf("AREA-1"));
        when(weather.getWeatherDate()).thenReturn(new WeatherDate(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0)));
        when(weather.getTemperature()).thenReturn(new Temperature(20));
        when(weather.getHumidity()).thenReturn(new Humidity(50));
        when(weather.getPressure()).thenReturn(new Pressure(1013));
        when(weather.getWindData()).thenReturn(new WindData(
                new WindDataDirection(180), new WindDataSpeed(10)));

        final String line = WeatherResponseFormatter.formatWeather(weather);
        final String joined = WeatherResponseFormatter.joinLines(List.of(line, line));

        assertTrue(line.contains("7|AREA-1|"));
        assertTrue(joined.contains("\n"));
    }
}
