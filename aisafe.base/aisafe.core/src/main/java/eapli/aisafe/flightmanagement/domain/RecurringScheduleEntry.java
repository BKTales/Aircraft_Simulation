package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.time.DayOfWeek;

import java.time.LocalTime;

@Embeddable
public class RecurringScheduleEntry implements ValueObject {

    private DayOfWeek dayOfWeek;
    private LocalTime time;

    public RecurringScheduleEntry(final DayOfWeek dayOfWeek, final LocalTime time) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
    }

    public DayOfWeek dayOfWeek() { return dayOfWeek; }
    public LocalTime time() { return time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecurringScheduleEntry that = (RecurringScheduleEntry) o;
        return dayOfWeek == that.dayOfWeek && java.util.Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dayOfWeek, time);
    }
}