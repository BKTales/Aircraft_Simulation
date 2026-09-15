package eapli.aisafe.flightmanagement.domain.simulation;

import java.util.Objects;

public final class ScheduleFlightStatus {

    private final String flightId;
    private final ValidationStatus validationStatus;

    public ScheduleFlightStatus(final String flightId, final ValidationStatus validationStatus) {
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        if (validationStatus == null) {
            throw new IllegalArgumentException("Validation status is required.");
        }
        this.flightId = flightId;
        this.validationStatus = validationStatus;
    }

    public String flightId() {
        return flightId;
    }

    public ValidationStatus validationStatus() {
        return validationStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ScheduleFlightStatus that = (ScheduleFlightStatus) o;
        return Objects.equals(flightId, that.flightId) && validationStatus == that.validationStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, validationStatus);
    }
}
