package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
@Access(AccessType.FIELD)
public class FlightSchedule implements ValueObject {

    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;

     protected FlightSchedule() {
        // ORM
    }

    public FlightSchedule(final LocalDateTime departure, final LocalDateTime arrival) {
        if (arrival.isBefore(departure)) {
            throw new IllegalArgumentException("Arrival cannot be before departure.");
        }
        this.scheduledDeparture = departure;
        this.scheduledArrival = arrival;
    }

    public LocalDateTime scheduledDeparture() { return scheduledDeparture; }
    public LocalDateTime scheduledArrival() { return scheduledArrival; }

    public boolean overlaps(final LocalDateTime intervalStart, final LocalDateTime intervalEnd) {
        if (intervalStart == null || intervalEnd == null) {
            return false;
        }
        return !scheduledDeparture.isAfter(intervalEnd) && !scheduledArrival.isBefore(intervalStart);
    }

    public static FlightSchedule valueOf(final LocalDateTime departure, final LocalDateTime arrival) {
        return new FlightSchedule(departure, arrival);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightSchedule that = (FlightSchedule) o;
        return java.util.Objects.equals(scheduledDeparture, that.scheduledDeparture) &&
                java.util.Objects.equals(scheduledArrival, that.scheduledArrival);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(scheduledDeparture, scheduledArrival);
    }
}
