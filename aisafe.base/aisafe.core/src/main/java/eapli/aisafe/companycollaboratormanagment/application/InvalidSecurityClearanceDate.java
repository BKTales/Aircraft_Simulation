package eapli.aisafe.companycollaboratormanagment.application;

public class InvalidSecurityClearanceDate extends IllegalArgumentException {
    public InvalidSecurityClearanceDate(String message) {
        super(message);
    }
}
