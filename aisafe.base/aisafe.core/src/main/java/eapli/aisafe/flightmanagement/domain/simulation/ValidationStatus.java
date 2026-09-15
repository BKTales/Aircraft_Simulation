package eapli.aisafe.flightmanagement.domain.simulation;

public enum ValidationStatus {
    PASS,
    FAIL;

    public static ValidationStatus fromReportValue(final String value) {
        if (value == null || value.isBlank()) {
            return FAIL;
        }
        return "PASS".equalsIgnoreCase(value.trim()) ? PASS : FAIL;
    }
}
