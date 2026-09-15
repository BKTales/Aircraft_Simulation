package eapli.aisafe.companycollaboratormanagment.application;

public class PilotHasActiveFlightPlansException extends RuntimeException {

    public PilotHasActiveFlightPlansException() {
        super("Cannot deactivate pilot with pending flight plans (DRAFT, SUBMITTED_FOR_SIMULATION, or SIM_APPROVED).");
    }
}
