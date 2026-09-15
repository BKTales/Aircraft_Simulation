package eapli.aisafe.routemanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class RouteSchedule implements ValueObject {

    @Column(name = "SCHEDULED_DEPARTURE")
    private LocalDate scheduledDeparture;

    @Column(name = "SCHEDULED_ARRIVAL")
    private LocalDate scheduledArrival;

    protected RouteSchedule() {
        // for ORM
    }

    public RouteSchedule(final LocalDate scheduledDeparture, final LocalDate scheduledArrival) {
        if (scheduledDeparture == null || scheduledArrival == null) {
            throw new IllegalArgumentException("Scheduled departure and arrival are required.");
        }
        if (scheduledArrival.isBefore(scheduledDeparture)) {
            throw new IllegalArgumentException("Scheduled arrival cannot be before scheduled departure.");
        }
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
    }

    public LocalDate scheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDate scheduledArrival() {
        return scheduledArrival;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RouteSchedule that)) {
            return false;
        }
        return Objects.equals(scheduledDeparture, that.scheduledDeparture)
                && Objects.equals(scheduledArrival, that.scheduledArrival);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledDeparture, scheduledArrival);
    }
}
