package eapli.aisafe.flightcontroloperatormanagement.application;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.time.util.CurrentTimeCalendars;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public class AddFlightControlOperatorController {
    private  AuthorizationService authz;
    private  FlightControlOperatorUserService flightControlOperatorUserService;
    private AirControlAreaRepository airControlAreaRepository;
    private TransactionalContext txCtx;
    private UserRepository userRepository;
    private FlightControlOperatorUserRepository flightControlOperatorUserRepository;
    private List<SystemUser> eligibleUsers = new ArrayList<>();


    public AddFlightControlOperatorController() {
        this(AuthzRegistry.authorizationService(),
                new FlightControlOperatorUserService());
        this.airControlAreaRepository =
                PersistenceContext.repositories().airControlArea();
        this.txCtx =
                PersistenceContext.repositories().newTransactionalContext();
        this.userRepository =
                PersistenceContext.repositories().users(this.txCtx);
        this.flightControlOperatorUserRepository =
                PersistenceContext.repositories().flightOperators(this.txCtx);
    }

    public AddFlightControlOperatorController(final AuthorizationService authz, final FlightControlOperatorUserService flightControlOperatorUserService){
        if(authz == null || flightControlOperatorUserService == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }

        this.authz = authz;
        this.flightControlOperatorUserService = flightControlOperatorUserService;
    }


    public String[] getAreaCodes() {
        return StreamSupport.stream(airControlAreaRepository.findAll().spliterator(), false)
                .map(company -> company.identity().toString())
                .toArray(String[]::new);
    }

    public List<String> getEligibleUsers() {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        eligibleUsers = flightControlOperatorUserService.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        return eligibleUsers.stream()
                .map(user -> user.email().toString())
                .toList();
    }

    public void addFCOToExistingUser(final String email,
                                     final String areaCode,
                                     final String securityData,
                                     final String skillsData,
                                     final String phoneNumber){
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        flightControlOperatorUserService.createFCOOnly(email, areaCode, securityData, skillsData, phoneNumber,
                eligibleUsers, flightControlOperatorUserRepository, txCtx, airControlAreaRepository);
    }

    private void addUser(final String username,
                        final String password,
                        final String firstName,
                        final String lastName,
                        final String email,
                        final Set<Role> roles,
                        final Calendar createdOn,
                        final String areaCode,
                        final String securityData,
                        final String skillsData,
                        final String phoneNumber) {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        flightControlOperatorUserService.createCollaboratorUser(username, password, firstName, lastName, email, roles,
                createdOn, areaCode, securityData, skillsData, phoneNumber, userRepository,
                flightControlOperatorUserRepository, txCtx, airControlAreaRepository);

    }


    public void addUser(final String username,
                        final String password,
                        final String firstName,
                        final String lastName,
                        final String email,
                        final Set<Role> roles, final String areaCode,
                        final String securityData,
                        final String skillsData,
                        final String phoneNumber) {

        // este método passa sempre pelo de cima, por isso só é preciso fazer a verificação se o user tem permissão no método addUser() de cima
        addUser(username, password, firstName, lastName, email, roles, CurrentTimeCalendars.now(), areaCode,securityData, skillsData, phoneNumber);
    }


}
