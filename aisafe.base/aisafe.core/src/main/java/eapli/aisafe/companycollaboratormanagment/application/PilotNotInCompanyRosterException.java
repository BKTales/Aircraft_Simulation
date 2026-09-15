package eapli.aisafe.companycollaboratormanagment.application;

public class PilotNotInCompanyRosterException extends RuntimeException {

    public PilotNotInCompanyRosterException() {
        super("Pilot does not belong to your company's roster.");
    }
}
