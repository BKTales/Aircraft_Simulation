package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;


public class ListCompanyCollaboratorUsersController {
    private final AuthorizationService authz;
    private final CompanyCollaboratorUserService collaboratorUserService;
    private final CompanyCollaboratorUserRepository collaboratorRepository;
    private final AirTransportCompanyRepository companyRepository;

    public ListCompanyCollaboratorUsersController(){
        this(AuthzRegistry.authorizationService(),
             new CompanyCollaboratorUserService(),
             PersistenceContext.repositories().collaborators(),
             PersistenceContext.repositories().airTransportCompanies());
    }


    public ListCompanyCollaboratorUsersController(
            AuthorizationService authz,
            CompanyCollaboratorUserService service,
            CompanyCollaboratorUserRepository repository,
            AirTransportCompanyRepository companyRepo) {

        if(authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }
        this.authz = authz;
        this.collaboratorUserService = service;
        this.collaboratorRepository = repository;
        this.companyRepository = companyRepo;
    }

    public Iterable<AirTransportCompany> allAirTransportCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        return companyRepository.findAll();
    }

    public Iterable<CompanyCollaboratorUser> activeATCCUsersForCompany(final String iataCode) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        final AirTransportCompany company = companyRepository
                .ofIdentity(IATACode.valueOf(iataCode))
                .orElseThrow(() -> new IllegalArgumentException("Air transport company not found: " + iataCode));

        return collaboratorUserService.findActiveCollaboratorsByCompany(collaboratorRepository, company);
    }
}
