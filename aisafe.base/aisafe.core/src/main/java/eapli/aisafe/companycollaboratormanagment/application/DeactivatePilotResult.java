package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;

/**
 * Rich return type for the deactivate-pilot use case.
 * Replaces thrown business exceptions so that callers (UI and TCP handler)
 * can switch on the outcome without coupling to exception types.
 */
public final class DeactivatePilotResult {

    public enum Outcome {
        SUCCESS,
        NOT_FOUND,
        NOT_IN_ROSTER,
        ALREADY_INACTIVE,
        HAS_ACTIVE_FLIGHTS
    }

    private final Outcome outcome;
    private final ResponsePilotCollaboratorDTO pilot;

    private DeactivatePilotResult(final Outcome outcome, final ResponsePilotCollaboratorDTO pilot) {
        this.outcome = outcome;
        this.pilot = pilot;
    }

    public static DeactivatePilotResult success(final PilotUser pilot) {
        return new DeactivatePilotResult(Outcome.SUCCESS, pilot.toDTO());
    }

    public static DeactivatePilotResult failure(final Outcome outcome) {
        return new DeactivatePilotResult(outcome, null);
    }

    public Outcome outcome() {
        return outcome;
    }

    /** Non-null only when {@code outcome == SUCCESS}. */
    public ResponsePilotCollaboratorDTO pilot() {
        return pilot;
    }
}
