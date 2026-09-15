package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Use-case controller for listing all registered airports.
 *
 * <p>Only users with the {@code BACKOFFICE_OPERATOR} role may invoke this use case.</p>
 *
 * @author aisafe team
 */
@UseCaseController
public class ListAirportsController {

    private final AuthorizationService authz;
    private final AirportRepository repository;

    public ListAirportsController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().airports());
    }

    public ListAirportsController(final AuthorizationService authz, final AirportRepository repository) {
        if(authz == null || repository == null){
            throw new IllegalArgumentException("Authorization service and repository are required.");
        }
        this.authz = authz;
        this.repository = repository;
    }

    /**
     * Returns all airports registered in the system.
     *
     * @return iterable of all {@link Airport} instances
     */
    public Iterable<Airport> allAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        return repository.findAll();
    }
}
