package eapli.aisafe.infrastructure.bootstrapers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.strings.util.Strings;
import eapli.framework.validations.Invariants;

@SuppressWarnings("squid:S106")
public class Bootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    private static final String ADMIN_PWD = TestDataConstants.PASSWORD1;
    private static final String ADMIN = "admin";

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();
    private final UserRepository userRepository = PersistenceContext.repositories().users();

    public static void main(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new NilPasswordPolicy(), new PlainTextEncoder());
        new Bootstrapper().execute();
        System.exit(0);
    }

    @Override
    public boolean execute() {
        final Action[] actions = {
                new EmailDomainsBootstrapper(),
                new MasterUsersBootstrapper(),
                new AirTransportCompaniesBootstrapper(),
                new AirControlAreasBootstrapper(),
                new AirportsBootstrapper(),
                new CompanyCollaboratorUserBootstrapper(),
                new ManufacturersBootstrapper(),
                new EngineModelsBootstrapper(),
                new AircraftModelsBootstrapper(),
                new AircraftBootstrapper(),
                new RoutesBootstrapper(),
                new PilotUserBootstrapper(),
                new FlightControlOperatorUserBootstrapper(),
                new FlightBootstrapper()
        };

        registerAdminUser();
        authenticateForBootstrapping();

        boolean ret = true;
        for (final Action boot : actions) {
            System.out.println("Bootstrapping " + nameOfEntity(boot) + "...");
            ret &= boot.execute();
        }
        return ret;
    }

    private boolean registerAdminUser() {
        final var userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
        userBuilder.withUsername(ADMIN)
                .withPassword(ADMIN_PWD)
                .withName("Jane", "Doe Admin")
                .withEmail("admin@aisafe.admin.com")
                .withRoles(AISafeRoles.ADMIN);
        final var newUser = userBuilder.build();

        try {
            final SystemUser admin = userRepository.save(newUser);
            return admin != null;
        } catch (ConcurrencyException | IntegrityViolationException e) {
            LOGGER.warn("Assuming {} already exists", newUser.username());
            LOGGER.trace("Assuming existing record", e);
            return false;
        } catch (jakarta.persistence.RollbackException e) {
            LOGGER.warn("Assuming {} already exists (rollback)", newUser.username());
            LOGGER.trace("Assuming existing record", e);
            return false;
        }
    }

    protected void authenticateForBootstrapping() {
        authenticationService.authenticate(ADMIN, ADMIN_PWD);
        Invariants.ensure(authz.hasSession());
    }

    private String nameOfEntity(final Action boot) {
        final var name = boot.getClass().getSimpleName();
        return Strings.left(name, name.length() - "Bootstrapper".length());
    }
}
