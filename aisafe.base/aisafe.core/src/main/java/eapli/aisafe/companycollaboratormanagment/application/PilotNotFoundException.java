package eapli.aisafe.companycollaboratormanagment.application;

public class PilotNotFoundException extends RuntimeException {

    public PilotNotFoundException() {
        super("Pilot not found in roster.");
    }
}
