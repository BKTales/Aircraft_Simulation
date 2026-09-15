package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Use-case controller for listing all registered air transport companies.
 *
 * <p>Only users with the {@code ADMIN} or {@code BACKOFFICE_OPERATOR} role may invoke this use case.</p>
 */
@UseCaseController
public class ListAirTransportCompaniesController {

    private final AuthorizationService authz;
    private final AirTransportCompanyRepository repository;

    public ListAirTransportCompaniesController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().airTransportCompanies());
    }

    public ListAirTransportCompaniesController(final AuthorizationService authz,
                                               final AirTransportCompanyRepository repository) {
        if(authz == null || repository == null) {
            throw new IllegalArgumentException("Authorization service and repository are required.");
        }
        this.authz = authz;
        this.repository = repository;
    }

    /**
     * Returns all air transport companies registered in the system.
     *
     * @return iterable of all {@link AirTransportCompany} instances
     */
    public Iterable<AirTransportCompany> allCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        return repository.findAll();
    }
}
