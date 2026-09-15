package eapli.aisafe.flightmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identity of a {@link FlightPlan}; 1:1 with {@link FlightDesignator} (one plan per flight).
 */
@Embeddable
public class FlightPlanId implements Serializable, Comparable<FlightPlanId> {

    private static final long serialVersionUID = 1L;

    @Column(name = "FLIGHT_CODE", nullable = false, length = 16)
    private String flightCode;

    protected FlightPlanId() {
        // ORM
    }

    private FlightPlanId(final String flightCode) {
        if (flightCode == null || flightCode.isBlank()) {
            throw new IllegalArgumentException("Flight code is required.");
        }
        this.flightCode = flightCode.trim();
    }

    public static FlightPlanId of(final FlightDesignator designator) {
        return new FlightPlanId(designator.toString());
    }

    public String flightCode() {
        return flightCode;
    }

    @Override
    public int compareTo(final FlightPlanId other) {
        return this.flightCode.compareTo(other.flightCode);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlightPlanId)) {
            return false;
        }
        final FlightPlanId that = (FlightPlanId) o;
        return Objects.equals(flightCode, that.flightCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightCode);
    }

    @Override
    public String toString() {
        return flightCode;
    }
}
