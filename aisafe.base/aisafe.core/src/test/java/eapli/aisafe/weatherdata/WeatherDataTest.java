package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.weatherdata.domain.Humidity;
import eapli.aisafe.weatherdata.domain.Pressure;
import eapli.aisafe.weatherdata.domain.Temperature;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.domain.WeatherDate;
import eapli.aisafe.weatherdata.domain.WeatherSection;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {

    @Test
    void testWeatherDataConstructionAndGetters() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        WeatherDate weatherDate = new WeatherDate(start, end);
        Humidity humidity = new Humidity(45.0);
        Pressure pressure = new Pressure(1013.25);
        Temperature temperature = new Temperature(23.5);
        WindData windData = new WindData(new WindDataDirection(450), new WindDataSpeed(7.5));
        WeatherSection section = new WeatherSection(new GeographicBoundary(List.of(
                new GeographicCoords(1, 1),
                new GeographicCoords(2, 1),
                new GeographicCoords(1, 2)
        )));

        AirControlArea area = new AirControlArea(
                new AirControlAreaName("Test Area"),
                new GeographicBoundary(List.of(
                        new GeographicCoords(0, 0),
                        new GeographicCoords(10, 0),
                        new GeographicCoords(10, 10),
                        new GeographicCoords(0, 10)
                )),
                new MinFuelRequirement(100)
        );

        WeatherData weatherData = new WeatherData(weatherDate, humidity, pressure, temperature, windData, section, area);

        assertEquals(weatherDate, weatherData.getWeatherDate());
        assertEquals(humidity, weatherData.getHumidity());
        assertEquals(pressure, weatherData.getPressure());
        assertEquals(temperature, weatherData.getTemperature());
        assertEquals(windData, weatherData.getWindData());
        assertEquals(section, weatherData.getWeatherSection());
    }

    @Test
    void testWeatherDataIdentityAndSameAs() {
        WeatherData weatherData = new WeatherData(
                new WeatherDate(LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                new Humidity(50),
                new Pressure(1000),
                new Temperature(20),
                new WindData(new WindDataDirection(45), new WindDataSpeed(5)),
                new WeatherSection(new GeographicBoundary(List.of(
                        new GeographicCoords(1, 1),
                        new GeographicCoords(2, 1),
                        new GeographicCoords(1, 2)
                ))),
                new AirControlArea(
                        new AirControlAreaName("Area A"),
                        new GeographicBoundary(List.of(
                                new GeographicCoords(0, 0),
                                new GeographicCoords(10, 0),
                                new GeographicCoords(10, 10),
                                new GeographicCoords(0, 10)
                        )),
                        new MinFuelRequirement(100)
                )
        );

        assertNull(weatherData.identity());
        assertTrue(weatherData.sameAs(weatherData));
        assertFalse(weatherData.sameAs(null));
    }

    @Test
    void testProtectedConstructorCoverage() {
        WeatherData weatherData = new WeatherData() {};
        assertNotNull(weatherData);
    }
}
