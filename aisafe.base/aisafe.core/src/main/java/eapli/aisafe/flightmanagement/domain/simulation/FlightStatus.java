package eapli.aisafe.flightmanagement.domain.simulation;

import java.util.Objects;

public final class FlightStatus {

    private final String flightId;
    private final FlightExecutionStatus executionStatus;

    public FlightStatus(final String flightId, final FlightExecutionStatus executionStatus) {
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        if (executionStatus == null) {
            throw new IllegalArgumentException("Execution status is required.");
        }
        this.flightId = flightId;
        this.executionStatus = executionStatus;
    }

    public String flightId() {
        return flightId;
    }

    public FlightExecutionStatus executionStatus() {
        return executionStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FlightStatus that = (FlightStatus) o;
        return Objects.equals(flightId, that.flightId) && executionStatus == that.executionStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, executionStatus);
    }
}
