package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

import java.util.ArrayList;
import java.util.List;

@Embeddable
public class FlightRecurringSchedule implements ValueObject {

    @ElementCollection
    @CollectionTable(name = "FLIGHT_RECURRING_ENTRIES")
    private List<RecurringScheduleEntry> entries = new ArrayList<>();


    public FlightRecurringSchedule(final List<RecurringScheduleEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Recurring schedule must have at least one entry.");
        }
        this.entries = new ArrayList<>(entries);
    }

    public List<RecurringScheduleEntry> entries() {
        return new ArrayList<>(this.entries);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightRecurringSchedule that = (FlightRecurringSchedule) o;
        return java.util.Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(entries);
    }
}
