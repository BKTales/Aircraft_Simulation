package eapli.aisafe.routemanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Embeddable
public class RouteRecurringSchedule implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @ElementCollection
    @CollectionTable(
            name = "ROUTE_RECURRING_SCHEDULE_ENTRY",
            joinColumns = @JoinColumn(name = "ROUTE_NAME", referencedColumnName = "ROUTE_NAME"))
    private List<RecurringScheduleEntry> entries = new ArrayList<>();

    protected RouteRecurringSchedule() {
        // for ORM
    }

    public RouteRecurringSchedule(final List<RecurringScheduleEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("At least one recurring schedule entry is required.");
        }

        final Set<DayOfWeek> days = new LinkedHashSet<>();
        for (final RecurringScheduleEntry entry : entries) {
            if (entry == null) {
                throw new IllegalArgumentException("Recurring schedule entries cannot be null.");
            }
            if (!days.add(entry.dayOfWeek())) {
                throw new IllegalArgumentException("Duplicate recurring day is not allowed: " + entry.dayOfWeek());
            }
        }
        this.entries = new ArrayList<>(entries);
    }

    public List<RecurringScheduleEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RouteRecurringSchedule that)) {
            return false;
        }
        return Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }
}
