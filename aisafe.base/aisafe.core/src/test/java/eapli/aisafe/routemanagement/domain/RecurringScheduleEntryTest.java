package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecurringScheduleEntryTest {

    @Test
    void ofCreatesEntry() {
        final RecurringScheduleEntry entry = RecurringScheduleEntry.of(DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.WEDNESDAY, entry.dayOfWeek());
        assertEquals("WEDNESDAY", entry.toString());
    }

    @Test
    void rejectsNullDay() {
        assertThrows(IllegalArgumentException.class, () -> new RecurringScheduleEntry(null));
    }

    @Test
    void equalsAndHashCode() {
        final RecurringScheduleEntry a = RecurringScheduleEntry.of(DayOfWeek.FRIDAY);
        final RecurringScheduleEntry b = RecurringScheduleEntry.of(DayOfWeek.FRIDAY);
        final RecurringScheduleEntry c = RecurringScheduleEntry.of(DayOfWeek.SATURDAY);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, "FRIDAY");
    }
}
