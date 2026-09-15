package eapli.aisafe.routemanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Objects;

@Embeddable
public class RecurringScheduleEntry implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "DAY_OF_WEEK", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    protected RecurringScheduleEntry() {
        // for ORM
    }

    public RecurringScheduleEntry(final DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week is required.");
        }
        this.dayOfWeek = dayOfWeek;
    }

    public static RecurringScheduleEntry of(final DayOfWeek dayOfWeek) {
        return new RecurringScheduleEntry(dayOfWeek);
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecurringScheduleEntry that)) {
            return false;
        }
        return dayOfWeek == that.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek);
    }

    @Override
    public String toString() {
        return dayOfWeek.name();
    }
}
