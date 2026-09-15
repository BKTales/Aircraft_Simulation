package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.companycollaboratormanagment.application.CompanyCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.time.format.DateTimeParseException;

public class CompanyCollaboratorUserBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyCollaboratorUserBootstrapper.class);

    private final CompanyCollaboratorUserService collaboratorService = new CompanyCollaboratorUserService();
    private final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();
    private final UserRepository userRepository = PersistenceContext.repositories().users(txCtx);
    private final CompanyCollaboratorUserRepository collaboratorUserRepository = PersistenceContext.repositories().collaborators(txCtx);
    private final AirTransportCompanyRepository airTransportCompanyRepository = PersistenceContext.repositories().airTransportCompanies();


    public boolean execute() {
        registerCollaborator("atcc1", "password123", "John", "Doe", "john.doe@example.com", "TP",
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, "2027-04-01", "2026-04-01", "+351999999999");
        registerCollaborator("atcc2", "password123", "Jane", "Smith", "jane.smith@example.com", "FR",
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, "2027-04-02", "2026-04-02", "+351999999998");
        registerCollaborator("atcc3", "password123", "Alice", "Johnson", "alice.johnson@example.com", "LH",
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, "2027-05-15", "2026-05-15", "+351999999997");

        return true;
    }


    private void registerCollaborator(final String username, final String password, final String firstName,
                                      final String lastName, final String email, final String companyIata,
                                      final Role authRole, final String securityExpiryDate, final String skillsData, final String phoneNumber) {

        Set<Role> roles = new HashSet<>();
        roles.add(authRole);

        if (userRepository.ofIdentity(Username.valueOf(username)).isPresent()) {
            LOGGER.debug("Assuming user {} already exists (skip)", username);
            return;
        }

        try {
            collaboratorService.createCollaboratorUser(username, password, firstName, lastName, email, roles,
                    Calendar.getInstance(), companyIata, securityExpiryDate, skillsData, phoneNumber, userRepository,
                    collaboratorUserRepository, txCtx, airTransportCompanyRepository);
        } catch (final IntegrityViolationException | ConcurrencyException e) {
            LOGGER.debug("That username is already in use.");
        } catch (jakarta.persistence.RollbackException e) {
            LOGGER.debug("Assuming user {} already exists (rollback)", username);
        } catch (DateTimeParseException e) {
            LOGGER.debug("The date format is invalid. Please use format YYYY-MM-DD (e.g. 2026-04-28).");
        }
    }

}
