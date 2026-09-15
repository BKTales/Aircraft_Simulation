package eapli.aisafe.companycollaboratormanagment.application;

public class InvalidPhoneFormat extends IllegalArgumentException {
    public InvalidPhoneFormat(String message) {
        super(message);
    }
}
