package eapli.aisafe.usermanagement.application;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.Optional;
import java.util.Set;

public class AddUserService {
    private final EmailDomainRepository emailDomainRepo;

    public AddUserService(){
        this.emailDomainRepo = PersistenceContext.repositories().emailDomains();
    }

    public AddUserService(final EmailDomainRepository emailDomainRepo) {
        this.emailDomainRepo = emailDomainRepo;
    }

    public AddUserValidationResult validateRegistration(final Set<Role> roles, final String email) {
        final AddUserValidationResult roleValidation = validateRegisterableRole(roles);
        if (!roleValidation.isValid()) {
            return roleValidation;
        }
        return validateEmailDomain(roles, email);
    }

    public AddUserValidationResult validateRegisterableRole(final Set<Role> roles) {
        final Role role = roles.stream().findFirst().orElse(null);

        if (role == null) {
            return AddUserValidationResult.noRoleSelected();
        }

        if (!AISafeRoles.isBackofficeRegisterable(role)) {
            return AddUserValidationResult.roleNotRegisterable(role);
        }

        return AddUserValidationResult.valid();
    }

    public AddUserValidationResult validateEmailDomain(final Set<Role> roles, final String email) {
        final String extractedEmailDomain = extractDomainFromEmail(email);
        final Role role = roles.stream().findFirst().orElse(null);

        if (role == null) {
            return AddUserValidationResult.noRoleSelected();
        }

        if (role.equals(AISafeRoles.BACKOFFICE_OPERATOR) ||
            role.equals(AISafeRoles.ADMIN) ||
            role.equals(AISafeRoles.WEATHER_PERSON)) {

            final Optional<EmailDomain> found = emailDomainRepo.findByDomain(extractedEmailDomain);

            if (found.isEmpty()) {
                return AddUserValidationResult.invalidEmailDomain(role);
            }
        }

        return AddUserValidationResult.valid();
    }

    public String extractDomainFromEmail(final String email) {
        return email.substring(email.indexOf('@') + 1);
    }
}
