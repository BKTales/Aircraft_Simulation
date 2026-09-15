package eapli.aisafe.companycollaboratormanagment.application;

public class PilotAlreadyInactiveException extends RuntimeException {

    public PilotAlreadyInactiveException() {
        super("Pilot is already inactive.");
    }
}
