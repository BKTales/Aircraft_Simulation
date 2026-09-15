package eapli.aisafe.flightmanagement.infrastructure.simulator;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorWeatherSnapshotExporterTest {

    private final SimulatorWeatherSnapshotExporter exporter = new SimulatorWeatherSnapshotExporter();

    @Test
    void exportsWindInMetresPerSecondAndBounds() throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 10, 8, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 10, 12, 0);
        final WeatherData weather = sampleWeather(42L, start, end, 270, 7.5);

        final String json = exporter.toJson("AREA-0", List.of(weather), List.of());

        assertTrue(json.contains("\"areaCode\": \"AREA-0\""));
        assertTrue(json.contains("\"windDirectionDeg\": 270"));
        assertTrue(json.contains("\"windSpeedMs\": 7.5"));
        assertTrue(json.contains("\"minLat\": 37"));
        assertTrue(json.contains("\"maxLat\": 38"));
        assertTrue(json.contains("\"startTimeS\": " + toEpoch(start)));
        assertTrue(json.contains("\"endTimeS\": " + toEpoch(end)));
    }

    @Test
    void mergesAttachedFlightWeatherWithoutDuplicates() throws Exception {
        final WeatherData shared = sampleWeather(7L,
                LocalDateTime.of(2026, 6, 10, 8, 0),
                LocalDateTime.of(2026, 6, 10, 12, 0),
                90, 12.0);
        final Flight flight = new Flight(
                new FlightDesignator("TP200"),
                "ROUTE-1",
                "CS-DEMO");
        flight.assignFlightPlan(FlightPlan.forFlight(
                flight.designator(),
                FlightPlanStatus.DRAFT,
                FuelLoad.valueOf(1000.0),
                SimulatorJsonTestFixtures.minimalSelfContainedJson("AREA-0")));
        flight.assignWeatherData(shared);

        final String json = exporter.toJson("AREA-0", List.of(shared), List.of(flight));

        final int first = json.indexOf("\"windDirectionDeg\": 90");
        final int last = json.lastIndexOf("\"windDirectionDeg\": 90");
        assertEquals(first, last);
    }

    private static WeatherData sampleWeather(final Long id,
                                             final LocalDateTime start,
                                             final LocalDateTime end,
                                             final int directionDeg,
                                             final double speedMs) throws Exception {
        final WeatherSection section = new WeatherSection(new GeographicBoundary(List.of(
                GeographicCoords.valueOf(37.0, -9.5),
                GeographicCoords.valueOf(37.0, -8.0),
                GeographicCoords.valueOf(38.0, -8.0),
                GeographicCoords.valueOf(38.0, -9.5)
        )));
        final AirControlArea area = new AirControlArea(
                new AirControlAreaName("Lisboa"),
                new GeographicBoundary(List.of(
                        GeographicCoords.valueOf(36.0, -10.0),
                        GeographicCoords.valueOf(36.0, -7.0),
                        GeographicCoords.valueOf(39.0, -7.0),
                        GeographicCoords.valueOf(39.0, -10.0)
                )),
                new MinFuelRequirement(100));
        final WeatherData weather = new WeatherData(
                new WeatherDate(start, end),
                new Humidity(50),
                new Pressure(1013),
                new Temperature(20),
                new WindData(WindDataDirection.valueOf(directionDeg), WindDataSpeed.valueOf(speedMs)),
                section,
                area);
        setIdentity(weather, id);
        return weather;
    }

    private static void setIdentity(final WeatherData weather, final Long id) throws Exception {
        final Field field = WeatherData.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(weather, id);
    }

    private static long toEpoch(final LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
