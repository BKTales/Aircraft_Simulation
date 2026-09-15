package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;
import eapli.aisafe.companycollaboratormanagment.application.PilotCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class PilotUserBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(PilotUserBootstrapper.class);

    private final PilotCollaboratorUserService pilotService = new PilotCollaboratorUserService();

    private final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();
    private final UserRepository userRepository = PersistenceContext.repositories().users(txCtx);
    private final PilotUserRepository pilotUserRepository = PersistenceContext.repositories().pilots(txCtx);
    private final AirTransportCompanyRepository airTransportCompanyRepository = PersistenceContext.repositories().airTransportCompanies();
    private final AircraftModelRepository aircraftModelRepository = PersistenceContext.repositories().aircraftModels();



    @Override
    public boolean execute() {
        registerCollaborator("pilot1", "password123", "John", "Doe", "pilot1.doe@example.com", "TP",
                AISafeRoles.PILOT, "2027-04-01", "2026-04-01", "+351999999999",
                List.of(
                        new PilotCertificationSpec("A320", "2026-01-01", "2028-12-31"),
                        new PilotCertificationSpec("A321", "2026-02-01", "2028-12-31"),
                        new PilotCertificationSpec("A380", "2026-01-01", "2028-12-31")
                ));
        registerCollaborator("pilot2", "password123", "Jane", "Smith", "pilot2.smith@example.com", "FR",
                AISafeRoles.PILOT, "2027-04-02", "2026-04-02","+351999999998",
                List.of(
                        new PilotCertificationSpec("B737", "2026-03-01", "2028-12-31")
                ));
        registerCollaborator("pilot3", "password123", "Alice", "Johnson", "pilot3.johnson@example.com", "LH",
                AISafeRoles.PILOT, "2027-05-15", "2026-05-15", "+351999999997",
                List.of(
                        new PilotCertificationSpec("A350", "2026-04-01", "2028-12-31")
                ));

        return true;
    }


    private void registerCollaborator(final String username, final String password, final String firstName,
                                      final String lastName, final String email, final String companyIata,
                                      final Role authRole, final String securityStartDate, final String skillsData, final String phoneNumber,
                                      final List<PilotCertificationSpec> certifications) {

        final AirTransportCompany company = airTransportCompanyRepository
                .ofIdentity(IATACode.valueOf(companyIata))
                .orElse(null);

        if (company == null) {
            LOGGER.debug("Company with IATA code {} not found, skipping pilot {}", companyIata, username);
            return;
        }

        final Set<Role> roles = new HashSet<>();
        roles.add(authRole);

        if (userRepository.ofIdentity(Username.valueOf(username)).isPresent()) {
            LOGGER.debug("Assuming user {} already exists (skip)", username);
            return;
        }

        try {
            pilotService.createPilotUser(username, password, firstName, lastName, email, roles,
                    Calendar.getInstance(), company, securityStartDate, skillsData, phoneNumber, certifications,
                    userRepository, pilotUserRepository, txCtx, aircraftModelRepository);
        } catch (final IntegrityViolationException | ConcurrencyException e) {
            LOGGER.debug("That username is already in use.");
        } catch (jakarta.persistence.RollbackException e) {
            LOGGER.debug("Assuming user {} already exists (rollback)", username);
        } catch (DateTimeParseException e) {
            LOGGER.debug("The date format is invalid. Please use format YYYY-MM-DD (e.g. 2026-04-28).");
        }
    }
}
