package eapli.aisafe.flightmanagement.domain.simulation;

import java.util.Objects;

public final class SafetyViolationEvent {

    private final String violationType;
    private final int step;
    private final int elapsedSeconds;
    private final String flightIdA;
    private final String positionA;
    private final String flightIdB;
    private final String positionB;

    public SafetyViolationEvent(final String violationType,
                                final int step,
                                final int elapsedSeconds,
                                final String flightIdA,
                                final String positionA,
                                final String flightIdB,
                                final String positionB) {
        if (violationType == null || violationType.isBlank()) {
            throw new IllegalArgumentException("Violation type is required.");
        }
        if (flightIdA == null || flightIdA.isBlank()) {
            throw new IllegalArgumentException("Flight A id is required.");
        }
        if (flightIdB == null || flightIdB.isBlank()) {
            throw new IllegalArgumentException("Flight B id is required.");
        }
        this.violationType = violationType;
        this.step = step;
        this.elapsedSeconds = elapsedSeconds;
        this.flightIdA = flightIdA;
        this.positionA = positionA;
        this.flightIdB = flightIdB;
        this.positionB = positionB;
    }

    public String violationType() {
        return violationType;
    }

    public int step() {
        return step;
    }

    public int elapsedSeconds() {
        return elapsedSeconds;
    }

    public String flightIdA() {
        return flightIdA;
    }

    public String positionA() {
        return positionA;
    }

    public String flightIdB() {
        return flightIdB;
    }

    public String positionB() {
        return positionB;
    }

    public String timestampLabel() {
        return "step " + step + " (" + elapsedSeconds + "s)";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SafetyViolationEvent that = (SafetyViolationEvent) o;
        return step == that.step
                && elapsedSeconds == that.elapsedSeconds
                && Objects.equals(violationType, that.violationType)
                && Objects.equals(flightIdA, that.flightIdA)
                && Objects.equals(positionA, that.positionA)
                && Objects.equals(flightIdB, that.flightIdB)
                && Objects.equals(positionB, that.positionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(violationType, step, elapsedSeconds, flightIdA, positionA, flightIdB, positionB);
    }
}
