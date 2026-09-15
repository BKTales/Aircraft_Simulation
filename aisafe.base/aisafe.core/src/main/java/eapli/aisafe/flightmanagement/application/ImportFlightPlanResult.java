package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FlightDesignator;

public final class ImportFlightPlanResult {

    private final boolean success;
    private final String message;
    private final FlightDesignator designator;

    private ImportFlightPlanResult(final boolean success,
                                   final String message,
                                   final FlightDesignator designator) {
        this.success = success;
        this.message = message;
        this.designator = designator;
    }

    public static ImportFlightPlanResult success(final FlightDesignator designator) {
        return new ImportFlightPlanResult(true,
                "Flight plan imported: " + designator + " (status DRAFT).", designator);
    }

    public static ImportFlightPlanResult failure(final String message) {
        return new ImportFlightPlanResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String message() {
        return message;
    }

    public FlightDesignator designator() {
        return designator;
    }
}
