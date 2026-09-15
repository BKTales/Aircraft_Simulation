package eapli.aisafe.usermanagement.application;

public class EmailDomainAlreadyExistsException extends RuntimeException {
    public EmailDomainAlreadyExistsException(String message) {
      super("The email domain " + message + " already exists.");
    }
}
