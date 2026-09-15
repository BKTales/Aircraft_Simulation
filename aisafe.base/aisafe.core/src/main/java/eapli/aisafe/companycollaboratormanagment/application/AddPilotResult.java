package eapli.aisafe.companycollaboratormanagment.application;
public final class AddPilotResult {

    public enum Outcome {
        SUCCESS,
        DUPLICATE_USERNAME,
        INVALID_INPUT,
        INVALID_ROLE,
        ERROR,
        AIRCRAFT_MODEL_NOT_FOUND,
        NO_CERTIFICATIONS,
        INVALID_DATE_FORMAT,
        INVALID_PHONE_FORMAT,
        INVALID_SKILLS_DATE,
        INVALID_SECURITY_DATE,
        SESSION_NOT_FOUND
    }

    private final Outcome outcome;
    private final String message;

    private AddPilotResult(final Outcome outcome, String message) {
        this.outcome = outcome;
        this.message = message;
    }


    public static AddPilotResult success() {
        return new AddPilotResult(Outcome.SUCCESS,null);
    }


    public static AddPilotResult failure(Outcome outcome) { return new AddPilotResult(outcome, null); }
    public static AddPilotResult failureWithMsg(Outcome outcome, String message) { return new AddPilotResult(outcome, message); }

    public boolean isSuccess() {
        return this.outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public String message() {
        return message;
    }
}