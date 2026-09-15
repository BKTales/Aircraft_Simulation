package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.CreatePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
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

public class AddPilotCollaboratorController {
    private final AuthorizationService authz;
    private final PilotCollaboratorUserService pilotService;
    private final CompanyCollaboratorUserService companyCollaboratorUserService;
    private AirTransportCompanyRepository airTransportCompanyRepository;
    private TransactionalContext txCtx;
    private UserRepository userRepository;
    private CompanyCollaboratorUserRepository collaboratorRepository;
    private PilotUserRepository pilotUserRepository;
    private AircraftModelRepository aircraftModelRepository;

    public AddPilotCollaboratorController() {
        this(AuthzRegistry.authorizationService(), new PilotCollaboratorUserService(), new CompanyCollaboratorUserService());
        this.airTransportCompanyRepository = PersistenceContext.repositories().airTransportCompanies();
        this.txCtx = PersistenceContext.repositories().newTransactionalContext();
        this.userRepository = PersistenceContext.repositories().users(this.txCtx);
        this.collaboratorRepository = PersistenceContext.repositories().collaborators(this.txCtx);
        this.pilotUserRepository = PersistenceContext.repositories().pilots(this.txCtx);
        this.aircraftModelRepository = PersistenceContext.repositories().aircraftModels();
    }

    private AddPilotCollaboratorController(final AuthorizationService authz,
                                           final PilotCollaboratorUserService pilotService,
                                           final CompanyCollaboratorUserService companyCollaboratorUserService) {
        if (authz == null || pilotService == null) {
            throw new IllegalArgumentException("Authorization service and pilot service are required.");
        }
        this.authz = authz;
        this.pilotService = pilotService;
        this.companyCollaboratorUserService = companyCollaboratorUserService;
    }

    public String[] getAircraftModelIds() {
        return pilotService.getAircraftModelIds(aircraftModelRepository);
    }


    public AddPilotResult addUser(final CreatePilotCollaboratorDTO dto) {
        return addUser(dto, CurrentTimeCalendars.now());
    }

    private AddPilotResult addUser(final CreatePilotCollaboratorDTO dto, final Calendar createdOn) {
        authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final AirTransportCompany company = currentCompany();
        final Set<Role> roles = Set.of(AISafeRoles.PILOT);
        return pilotService.createPilotUser(dto.getUsername(), dto.getPassword(),
                dto.getFirstName(), dto.getLastName(), dto.getEmail(),
                roles, createdOn, company,
                dto.getSecurityClearanceStartDate(), dto.getSkillDate(), dto.getPhoneNumber(),
                dto.getCertifications(), userRepository,
                pilotUserRepository, txCtx, aircraftModelRepository);
    }

    public AirTransportCompany currentCompany() {
        final SystemUser user = authz.session()
                .orElseThrow()
                .authenticatedUser();
        final CompanyCollaboratorUser userCollab = companyCollaboratorUserService.findATCC(collaboratorRepository, user.username());
        return userCollab.airTransportCompany();
    }
}