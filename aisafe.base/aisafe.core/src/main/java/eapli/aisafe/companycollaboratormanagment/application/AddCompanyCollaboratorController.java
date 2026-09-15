package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
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

public class AddCompanyCollaboratorController {
    private final AuthorizationService authz;
    private final CompanyCollaboratorUserService collaboratorUserService;
    private AirTransportCompanyRepository airTransportCompanyRepository;
    private TransactionalContext txCtx;
    private UserRepository userRepository;
    private CompanyCollaboratorUserRepository collaboratorRepository;
    private List<SystemUser> eligibleUsers = new ArrayList<>();

    public AddCompanyCollaboratorController() {
        this(AuthzRegistry.authorizationService(), new CompanyCollaboratorUserService());
        this.airTransportCompanyRepository = PersistenceContext.repositories().airTransportCompanies();
        this.txCtx = PersistenceContext.repositories().newTransactionalContext();
        this.userRepository = PersistenceContext.repositories().users(this.txCtx);
        this.collaboratorRepository = PersistenceContext.repositories().collaborators(this.txCtx);
    }

    private AddCompanyCollaboratorController(final AuthorizationService authz,
                                             final CompanyCollaboratorUserService collaboratorUserService) {
        if (authz == null || collaboratorUserService == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }
        this.authz = authz;
        this.collaboratorUserService = collaboratorUserService;
    }

    public List<String> getEligibleUsers() {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        eligibleUsers = collaboratorUserService.findEligibleUsersForATCC(userRepository, collaboratorRepository);
        return eligibleUsers.stream()
                .map(user -> user.email().toString())
                .toList();
    }

    public String[] getCompanyIds() {
        return StreamSupport.stream(airTransportCompanyRepository.findAll().spliterator(), false)
                .map(company -> company.identity().toString())
                .toArray(String[]::new);
    }

    public void addATCCToExistingUser(final String email, final String companyId,
                                      final String securityData, final String skillsData, final String phoneNumber) {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        collaboratorUserService.createATCCOnly(email, companyId, securityData, skillsData, phoneNumber,
                eligibleUsers, collaboratorRepository, txCtx, airTransportCompanyRepository);
    }

    public void addUser(final String username, final String password, final String firstName,
                        final String lastName, final String email, final Set<Role> roles,
                        final String companyId, final String securityData, final String skillsData, final String phoneNumber) {
        addUser(username, password, firstName, lastName, email, roles,
                CurrentTimeCalendars.now(), companyId, securityData, skillsData, phoneNumber);
    }

    private void addUser(final String username, final String password, final String firstName,
                         final String lastName, final String email, final Set<Role> roles,
                         final Calendar createdOn, final String companyId,
                         final String securityData, final String skillsData, final String phoneNumber) {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        collaboratorUserService.createCollaboratorUser(username, password, firstName, lastName, email,
                roles, createdOn, companyId, securityData, skillsData, phoneNumber,
                userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository);
    }
}
