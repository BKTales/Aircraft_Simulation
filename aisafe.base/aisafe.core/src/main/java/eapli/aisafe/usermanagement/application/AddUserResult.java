package eapli.aisafe.usermanagement.application;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Optional;

public final class AddUserResult {

    public enum Outcome {
        SUCCESS,
        NO_ROLE_SELECTED,
        ROLE_NOT_REGISTERABLE,
        INVALID_EMAIL_DOMAIN,
        UNAUTHORIZED,
        DUPLICATE_USER
    }

    private final Outcome outcome;
    private final SystemUser user;
    private final String message;

    private AddUserResult(final Outcome outcome, final SystemUser user, final String message) {
        this.outcome = outcome;
        this.user = user;
        this.message = message;
    }

    public static AddUserResult success(final SystemUser user) {
        return new AddUserResult(Outcome.SUCCESS, user, null);
    }

    public static AddUserResult unauthorized() {
        return new AddUserResult(Outcome.UNAUTHORIZED, null, "Unauthorized");
    }

    public static AddUserResult duplicateUser() {
        return new AddUserResult(Outcome.DUPLICATE_USER, null, "That username is already in use.");
    }

    public static AddUserResult fromValidation(final AddUserValidationResult validation) {
        return new AddUserResult(mapValidationOutcome(validation.outcome()), null, validation.message());
    }

    private static Outcome mapValidationOutcome(final AddUserValidationResult.Outcome validationOutcome) {
        return switch (validationOutcome) {
            case VALID -> Outcome.SUCCESS;
            case NO_ROLE_SELECTED -> Outcome.NO_ROLE_SELECTED;
            case ROLE_NOT_REGISTERABLE -> Outcome.ROLE_NOT_REGISTERABLE;
            case INVALID_EMAIL_DOMAIN -> Outcome.INVALID_EMAIL_DOMAIN;
        };
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public Optional<SystemUser> user() {
        return Optional.ofNullable(user);
    }

    public String message() {
        return message;
    }
}
