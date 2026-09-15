package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteRecurringScheduleTest {

    @Test
    void acceptsDistinctDays() {
        final RouteRecurringSchedule schedule = new RouteRecurringSchedule(List.of(
                RecurringScheduleEntry.of(DayOfWeek.MONDAY),
                RecurringScheduleEntry.of(DayOfWeek.FRIDAY)));

        assertEquals(2, schedule.entries().size());
        assertEquals(DayOfWeek.MONDAY, schedule.entries().get(0).dayOfWeek());
    }

    @Test
    void entriesAreUnmodifiable() {
        final RouteRecurringSchedule schedule = new RouteRecurringSchedule(
                List.of(RecurringScheduleEntry.of(DayOfWeek.TUESDAY)));
        assertThrows(UnsupportedOperationException.class, () -> schedule.entries().add(
                RecurringScheduleEntry.of(DayOfWeek.WEDNESDAY)));
    }

    @Test
    void rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new RouteRecurringSchedule(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new RouteRecurringSchedule(null));
    }

    @Test
    void rejectsNullEntry() {
        final List<RecurringScheduleEntry> entries = new ArrayList<>();
        entries.add(RecurringScheduleEntry.of(DayOfWeek.MONDAY));
        entries.add(null);
        assertThrows(IllegalArgumentException.class, () -> new RouteRecurringSchedule(entries));
    }

    @Test
    void rejectsDuplicateDays() {
        assertThrows(IllegalArgumentException.class,
                () -> new RouteRecurringSchedule(List.of(
                        RecurringScheduleEntry.of(DayOfWeek.MONDAY),
                        RecurringScheduleEntry.of(DayOfWeek.MONDAY))));
    }

    @Test
    void equalsAndHashCode() {
        final List<RecurringScheduleEntry> days = List.of(RecurringScheduleEntry.of(DayOfWeek.MONDAY));
        final RouteRecurringSchedule a = new RouteRecurringSchedule(days);
        final RouteRecurringSchedule b = new RouteRecurringSchedule(days);
        final RouteRecurringSchedule c = new RouteRecurringSchedule(
                List.of(RecurringScheduleEntry.of(DayOfWeek.TUESDAY)));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertTrue(a.equals(a));
    }
}
