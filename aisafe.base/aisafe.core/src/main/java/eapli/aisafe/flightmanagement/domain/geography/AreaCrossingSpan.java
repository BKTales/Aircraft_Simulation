package eapli.aisafe.flightmanagement.domain.geography;

/**
 * Portion of a leg path that lies inside an air control area polygon.
 * Fractions are measured along the full leg polyline (0 = leg start, 1 = leg end).
 */
public record AreaCrossingSpan(
        int legIndex,
        double entryFraction,
        double exitFraction,
        double entryLatitude,
        double entryLongitude,
        double exitLatitude,
        double exitLongitude) {

    public boolean isFullLeg() {
        return entryFraction <= 1e-6 && exitFraction >= 1.0 - 1e-6;
    }

    public double spanFraction() {
        return Math.max(0.0, exitFraction - entryFraction);
    }
}
