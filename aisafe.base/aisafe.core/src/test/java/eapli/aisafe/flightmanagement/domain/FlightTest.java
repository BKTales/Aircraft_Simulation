package eapli.aisafe.flightmanagement.domain;

import eapli.aisafe.weatherdata.domain.WeatherData;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightTest {

    private Flight createSubject(String designator) {
        return new Flight(
                new FlightDesignator(designator),
                "TP123",
                "CS-TNH"
        );
    }

    @Test
    void ensureFlightIsCreatedWithValidData() {
        Flight subject = createSubject("TP1234A");
        assertNotNull(subject);
        assertEquals(new FlightDesignator("TP1234A"), subject.identity());
        assertEquals("CS-TNH", subject.aircraftRegistration());
        assertNull(subject.schedule());
    }

    @Test
    void ensureSameAsCoversAllBranches() {
        String designator = "TP1234A";
        Flight a = createSubject(designator);
        Flight b = createSubject(designator);
        Flight c = createSubject("TP9999B");


        assertTrue(a.sameAs(b));
        assertFalse(a.sameAs(c));
        assertFalse(a.sameAs(null));
        assertFalse(a.sameAs(new Object()));
        assertTrue(a.sameAs(a));
    }

    @Test
    void ensureIdentityReturnsCorrectDesignator() {
        FlightDesignator designator = new FlightDesignator("TP1234A");
        Flight subject = new Flight(designator, "R01", "REG01");

        assertEquals(designator, subject.identity());
    }

    @Test
    void ensureAssignAndGetScheduleWorksCorrectly() {
        Flight subject = createSubject("TP1234A");

        LocalDateTime departure = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 6, 1, 12, 0);
        FlightSchedule schedule = new FlightSchedule(departure, arrival);


        subject.assignSchedule(schedule);

        assertNotNull(subject.schedule());
        assertEquals(schedule, subject.schedule());
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<Flight> constructor = Flight.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Flight instance = constructor.newInstance();


        assertNotNull(instance);
        assertNull(instance.identity());
        assertNull(instance.aircraftRegistration());
        assertNull(instance.schedule());
    }

    @Test
    void ensureAssignWeatherDataResetsPlanToDraftWhenSimApproved() {
        final Flight subject = createSubject("TP1234A");
        subject.assignFlightPlan(FlightPlan.forFlight(
                new FlightDesignator("TP1234A"),
                FlightPlanStatus.SIM_APPROVED,
                new FuelLoad(1000.0),
                "{\"ID\":1}"
        ));
        final WeatherData newWeather = mockWeather(8L);

        subject.assignWeatherData(newWeather);

        assertEquals(newWeather, subject.weatherData());
        assertEquals(FlightPlanStatus.DRAFT, subject.flightPlan().status());
    }

    @Test
    void ensureAssignWeatherDataFailsWithoutPlan() {
        final Flight subject = createSubject("TP1234A");
        assertThrows(IllegalStateException.class, () -> subject.assignWeatherData(mockWeather(2L)));
    }

    @Test
    void ensureAssignWeatherDataRejectsNull() {
        final Flight subject = createSubject("TP1234A");
        subject.assignFlightPlan(FlightPlan.forFlight(
                new FlightDesignator("TP1234A"),
                FlightPlanStatus.DRAFT,
                new FuelLoad(1000.0),
                "{\"ID\":1}"
        ));
        assertThrows(IllegalArgumentException.class, () -> subject.assignWeatherData(null));
    }

    private static WeatherData mockWeather(final Long id) {
        final WeatherData weatherData = mock(WeatherData.class);
        when(weatherData.identity()).thenReturn(id);
        return weatherData;
    }

    @Test
    void ensureHasPlanReadyAndScheduleOverlapBranches() {
        final Flight subject = createSubject("TP1234A");
        assertFalse(subject.hasPlanReadyForSimulation());
        assertFalse(subject.scheduleOverlaps(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    }
}