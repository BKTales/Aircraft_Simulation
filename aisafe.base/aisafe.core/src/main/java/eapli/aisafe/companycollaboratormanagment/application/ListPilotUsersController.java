package eapli.aisafe.companycollaboratormanagment.application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;



public class ListPilotUsersController {
    private final AuthorizationService authz;
    private final PilotCollaboratorUserService pilotUserService;
    private final CompanyCollaboratorUserService collaboratorUserService;
    private final PilotUserRepository pilotUserRepository;
    private final CompanyCollaboratorUserRepository collaboratorUserRepository;
    private final AirTransportCompanyRepository companyRepository;

    public ListPilotUsersController(){
        this(AuthzRegistry.authorizationService(),
             new PilotCollaboratorUserService(),
             new CompanyCollaboratorUserService(),
             PersistenceContext.repositories().collaborators(),
             PersistenceContext.repositories().pilots(),
             PersistenceContext.repositories().airTransportCompanies());
    }


    public ListPilotUsersController(
            AuthorizationService authz,
            PilotCollaboratorUserService service,
            CompanyCollaboratorUserService collaboratorUserService,
            CompanyCollaboratorUserRepository companyCollaboratorUserRepository,
            PilotUserRepository repository,
            AirTransportCompanyRepository companyRepo) {

        if(authz == null || service == null || collaboratorUserService == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }
        this.authz = authz;
        this.pilotUserService = service;
        this.collaboratorUserService = collaboratorUserService;
        this.pilotUserRepository = repository;
        this.companyRepository = companyRepo;
        this.collaboratorUserRepository = companyCollaboratorUserRepository;
    }

    public Iterable<AirTransportCompany> allAirTransportCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        return companyRepository.findAll();
    }

    public ListPilotsResult activePilotUsersForCompany() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        SystemUser user = authz.session()
                .orElseThrow()
                .authenticatedUser();
        CompanyCollaboratorUser userCollab = collaboratorUserService.findATCC(collaboratorUserRepository,user.username());
        final AirTransportCompany company = userCollab.airTransportCompany();
        return pilotUserService.findActivePilotsByCompany(pilotUserRepository, company);
    }
}
