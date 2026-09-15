package eapli.aisafe.flightmanagement.domain;

public enum FlightPlanStatus {
    DRAFT, SUBMITTED_FOR_SIMULATION, SIM_APPROVED, SIM_REJECTED;

    /** US080: replace existing plan without user confirmation. */
    public boolean allowsSilentReplacement() {
        return this == DRAFT || this == SIM_REJECTED;
    }
}