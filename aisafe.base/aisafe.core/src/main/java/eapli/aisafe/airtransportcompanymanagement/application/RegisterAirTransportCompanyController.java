package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirTransportCompanyController {

    private final AuthorizationService authz;
    private final AirTransportCompanyService service;

    public RegisterAirTransportCompanyController() {
        this(AuthzRegistry.authorizationService(),
                new AirTransportCompanyService(PersistenceContext.repositories().airTransportCompanies()));
    }

    public RegisterAirTransportCompanyController(final AuthorizationService authz,
                                                 final AirTransportCompanyService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and company service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public AirTransportCompany registerCompany(final String name, final String iataCode, final String icaoCode) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        return service.registerCompany(name, iataCode, icaoCode);
    }
}
