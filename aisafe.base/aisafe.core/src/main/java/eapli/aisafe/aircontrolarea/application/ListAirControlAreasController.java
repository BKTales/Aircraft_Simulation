package eapli.aisafe.aircontrolarea.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

public class ListAirControlAreasController {

    private final AuthorizationService authz;
    private final AirControlAreaRepository repository;

    public ListAirControlAreasController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().airControlArea());
    }

    public ListAirControlAreasController(final AuthorizationService authz,
                                         final AirControlAreaRepository repository) {
        if (authz == null || repository == null) {
            throw new IllegalArgumentException("Authorization service and repository are required.");
        }
        this.authz = authz;
        this.repository = repository;
    }

    public Iterable<AirControlArea> allAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR, AISafeRoles.WEATHER_PERSON);

        return repository.findAll();
    }
}
