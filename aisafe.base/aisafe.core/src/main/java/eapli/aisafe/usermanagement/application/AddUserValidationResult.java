package eapli.aisafe.usermanagement.application;

import eapli.framework.infrastructure.authz.domain.model.Role;

public final class AddUserValidationResult {

    public enum Outcome {
        VALID,
        NO_ROLE_SELECTED,
        ROLE_NOT_REGISTERABLE,
        INVALID_EMAIL_DOMAIN
    }

    private final Outcome outcome;
    private final String message;

    private AddUserValidationResult(final Outcome outcome, final String message) {
        this.outcome = outcome;
        this.message = message;
    }

    public static AddUserValidationResult valid() {
        return new AddUserValidationResult(Outcome.VALID, null);
    }

    public static AddUserValidationResult noRoleSelected() {
        return new AddUserValidationResult(Outcome.NO_ROLE_SELECTED, "The selected role is null!");
    }

    public static AddUserValidationResult roleNotRegisterable(final Role role) {
        return new AddUserValidationResult(
                Outcome.ROLE_NOT_REGISTERABLE,
                "Role " + role + " cannot be registered via backoffice user management");
    }

    public static AddUserValidationResult invalidEmailDomain(final Role role) {
        return new AddUserValidationResult(
                Outcome.INVALID_EMAIL_DOMAIN,
                "The provided email doesn't contain a domain that is in the list of valid email domains for "
                        + role + "!");
    }

    public boolean isValid() {
        return outcome == Outcome.VALID;
    }

    public Outcome outcome() {
        return outcome;
    }

    public String message() {
        return message;
    }
}
