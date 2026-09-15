package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FlightScheduleTest {

    @Test
    void ensureFlightScheduleIsCreatedWithValidDates() {
        LocalDateTime departure = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2024, 6, 1, 12, 0);

        FlightSchedule subject = new FlightSchedule(departure, arrival);

        assertEquals(departure, subject.scheduledDeparture());
        assertEquals(arrival, subject.scheduledArrival());
    }

    @Test
    void ensureConstructorThrowsExceptionForInvalidDates() {
        LocalDateTime departure = LocalDateTime.of(2024, 6, 1, 12, 0);
        LocalDateTime arrivalBefore = LocalDateTime.of(2024, 6, 1, 10, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new FlightSchedule(departure, arrivalBefore));
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        LocalDateTime d1 = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime a1 = LocalDateTime.of(2024, 6, 1, 12, 0);

        FlightSchedule s1 = new FlightSchedule(d1, a1);
        FlightSchedule s2 = new FlightSchedule(d1, a1);
        FlightSchedule s3 = new FlightSchedule(d1, a1.plusHours(1));

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertEquals(s1, s1);
        assertNotEquals(null, s1);
        assertNotEquals("Não é um FlightSchedule", s1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<FlightSchedule> constructor = FlightSchedule.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        FlightSchedule instance = constructor.newInstance();
        assertNotNull(instance);
        assertNull(instance.scheduledDeparture());
        assertNull(instance.scheduledArrival());
    }
}