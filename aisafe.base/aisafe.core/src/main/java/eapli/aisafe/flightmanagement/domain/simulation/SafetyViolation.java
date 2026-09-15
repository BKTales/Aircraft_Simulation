package eapli.aisafe.flightmanagement.domain.simulation;

import java.util.Objects;

public final class SafetyViolation {

    private final String flightId;
    private final String timestamp;
    private final String position;

    public SafetyViolation(final String flightId, final String timestamp, final String position) {
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        this.flightId = flightId;
        this.timestamp = timestamp;
        this.position = position;
    }

    public String flightId() {
        return flightId;
    }

    public String timestamp() {
        return timestamp;
    }

    public String position() {
        return position;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SafetyViolation that = (SafetyViolation) o;
        return Objects.equals(flightId, that.flightId)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, timestamp, position);
    }
}
