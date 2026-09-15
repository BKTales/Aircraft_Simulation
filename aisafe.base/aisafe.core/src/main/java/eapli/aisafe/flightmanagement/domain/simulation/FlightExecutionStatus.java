package eapli.aisafe.flightmanagement.domain.simulation;

public enum FlightExecutionStatus {
    SUCCESS,
    COLLISION,
    OUT_OF_FUEL,
    LOW_ALTITUDE,
    OTHER;

    public static FlightExecutionStatus fromReportValue(final String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        final String normalized = value.trim().toUpperCase().replace(' ', '_');
        try {
            return FlightExecutionStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }

    public String userMessage() {
        return switch (this) {
            case SUCCESS -> "Flight completed successfully.";
            case OUT_OF_FUEL -> "Aircraft ran out of fuel.";
            case LOW_ALTITUDE -> "Aircraft flew below minimum safe altitude.";
            case COLLISION -> "Aircraft collision detected.";
            case OTHER -> "Simulation ended abnormally.";
        };
    }
}
