package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import java.util.Collections;

public final class ListPilotsResult {

    public enum Outcome {
        SUCCESS,
        ERROR
    }

    private final Outcome outcome;
    private final Iterable<ResponsePilotCollaboratorDTO> pilots;
    private final String message;

    private ListPilotsResult(final Outcome outcome, final Iterable<ResponsePilotCollaboratorDTO> pilots, final String message) {
        this.outcome = outcome;
        this.pilots = pilots != null ? pilots : Collections.emptyList();
        this.message = message;
    }

    public static ListPilotsResult success(final Iterable<ResponsePilotCollaboratorDTO> pilots) {
        return new ListPilotsResult(Outcome.SUCCESS, pilots, null);
    }

    public static ListPilotsResult failure(final Outcome outcome, final String message) {
        return new ListPilotsResult(outcome, null, message);
    }

    public boolean isSuccess() {
        return this.outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public Iterable<ResponsePilotCollaboratorDTO> pilots() {
        return pilots;
    }

    public String message() {
        return message;
    }
}