package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.flightcontroloperatormanagement.application.FlightControlOperatorUserService;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
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
import java.util.Set;

public class FlightControlOperatorUserBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightControlOperatorUserBootstrapper.class);

    private final FlightControlOperatorUserService flightControlOperatorUserService = new FlightControlOperatorUserService();
    private final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();
    private final UserRepository userRepository = PersistenceContext.repositories().users(txCtx);
    private final FlightControlOperatorUserRepository flightControlOperatorUserRepository = PersistenceContext.repositories().flightOperators(txCtx);
    private final AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().airControlArea();


    public boolean execute() {
        registerCollaborator("fco1", "password123", "Johnn", "Doe", "johnn.doe@example.com", "AREA-0", "FLIGHT_CONTROL_OPERATOR", "2027-04-01", "2026-04-01","+351999999999");
        registerCollaborator("fco2", "password123", "Janee", "Smith", "janee.smith@example.com", "AREA-0", "FLIGHT_CONTROL_OPERATOR", "2027-04-02", "2026-04-02","+351999999998");
        registerCollaborator("fco3", "password123", "Alicee", "Johnson", "alicee.johnson@example.com", "AREA-6", "FLIGHT_CONTROL_OPERATOR", "2027-05-15","2026-05-15","+351999999997");

        return true;
    }


    private void registerCollaborator(final String username, final String password, final String firstName,
                                      final String lastName, final String email, final String areaCode,
                                      final String role, final String securityStartDate, final String skilldata, final String phoneNumber) {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.valueOf(role));

        if (userRepository.ofIdentity(Username.valueOf(username)).isPresent()) {
            LOGGER.debug("Assuming user {} already exists (skip)", username);
            return;
        }

        try {
            flightControlOperatorUserService.createCollaboratorUser(username, password, firstName, lastName, email, roles,
                    Calendar.getInstance(), areaCode, securityStartDate, skilldata, phoneNumber, userRepository,
                    flightControlOperatorUserRepository, txCtx, airControlAreaRepository);
        } catch (final IntegrityViolationException | ConcurrencyException e) {
            LOGGER.debug("That username is already in use.");
        } catch (jakarta.persistence.RollbackException e) {
            LOGGER.debug("Assuming user {} already exists (rollback)", username);
        } catch (DateTimeParseException e) {
            LOGGER.debug("The date format is invalid. Please use format YYYY-MM-DD (e.g. 2026-04-28).");
        }
    }



}
