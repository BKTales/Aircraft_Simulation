package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FlightRecurringScheduleTest {

    private RecurringScheduleEntry constructorHelper() {
        return new RecurringScheduleEntry(DayOfWeek.MONDAY, LocalTime.of(10, 0));
    }

    @Test
    void ensureFlightRecurringScheduleIsCreatedWithEntries() {
        List<RecurringScheduleEntry> entries = Arrays.asList(constructorHelper());
        FlightRecurringSchedule subject = new FlightRecurringSchedule(entries);

        assertNotNull(subject);
        assertEquals(1, subject.entries().size());
    }

    @Test
    void ensureConstructorThrowsExceptionForInvalidEntries() {
        // Cobre entries == null
        assertThrows(IllegalArgumentException.class, () -> new FlightRecurringSchedule(null));

        // Cobre entries.isEmpty()
        assertThrows(IllegalArgumentException.class, () -> new FlightRecurringSchedule(new ArrayList<>()));
    }

    @Test
    void ensureEntriesReturnsDefensiveCopy() {
        List<RecurringScheduleEntry> list = new ArrayList<>();
        list.add(constructorHelper());

        FlightRecurringSchedule subject = new FlightRecurringSchedule(list);
        List<RecurringScheduleEntry> returnedList = subject.entries();

        returnedList.add(new RecurringScheduleEntry(DayOfWeek.TUESDAY, LocalTime.of(12, 0)));

        assertNotEquals(returnedList.size(), subject.entries().size());
        assertEquals(1, subject.entries().size());
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        RecurringScheduleEntry entry = constructorHelper();
        List<RecurringScheduleEntry> l1 = Arrays.asList(entry);
        List<RecurringScheduleEntry> l2 = Arrays.asList(new RecurringScheduleEntry(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        List<RecurringScheduleEntry> l3 = Arrays.asList(new RecurringScheduleEntry(DayOfWeek.SUNDAY, LocalTime.of(20, 0)));

        FlightRecurringSchedule s1 = new FlightRecurringSchedule(l1);
        FlightRecurringSchedule s2 = new FlightRecurringSchedule(l2);
        FlightRecurringSchedule s3 = new FlightRecurringSchedule(l3);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        assertNotEquals(s1, s3);
    }


}