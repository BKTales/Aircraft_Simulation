package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class RecurringScheduleEntryTest {

    @Test
    void ensureEntryIsCreatedWithValidData() {
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime time = LocalTime.of(14, 30);

        RecurringScheduleEntry subject = new RecurringScheduleEntry(day, time);

        assertEquals(day, subject.dayOfWeek());
        assertEquals(time, subject.time());
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        RecurringScheduleEntry e1 = new RecurringScheduleEntry(DayOfWeek.FRIDAY, LocalTime.of(10, 0));
        RecurringScheduleEntry e2 = new RecurringScheduleEntry(DayOfWeek.FRIDAY, LocalTime.of(10, 0));
        RecurringScheduleEntry e3 = new RecurringScheduleEntry(DayOfWeek.SATURDAY, LocalTime.of(10, 0));


        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());

        assertNotEquals(e1, e3);
    }


}