package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteScheduleTest {

    @Test
    void acceptsValidDates() {
        final LocalDate departure = LocalDate.of(2026, 6, 1);
        final LocalDate arrival = LocalDate.of(2026, 6, 2);
        final RouteSchedule schedule = new RouteSchedule(departure, arrival);

        assertEquals(departure, schedule.scheduledDeparture());
        assertEquals(arrival, schedule.scheduledArrival());
    }

    @Test
    void acceptsSameDayDepartureAndArrival() {
        final LocalDate day = LocalDate.of(2026, 6, 1);
        final RouteSchedule schedule = new RouteSchedule(day, day);
        assertEquals(day, schedule.scheduledArrival());
    }

    @Test
    void rejectsNullDates() {
        final LocalDate day = LocalDate.of(2026, 6, 1);
        assertThrows(IllegalArgumentException.class, () -> new RouteSchedule(null, day));
        assertThrows(IllegalArgumentException.class, () -> new RouteSchedule(day, null));
    }

    @Test
    void rejectsArrivalBeforeDeparture() {
        assertThrows(IllegalArgumentException.class,
                () -> new RouteSchedule(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 1)));
    }

    @Test
    void equalsAndHashCode() {
        final RouteSchedule a = new RouteSchedule(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
        final RouteSchedule b = new RouteSchedule(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
        final RouteSchedule c = new RouteSchedule(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "not a schedule");
    }
}
