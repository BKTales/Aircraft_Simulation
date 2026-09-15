package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class ListAircraftModelsController {

    private final AuthorizationService authz;
    private final AircraftModelRepository repo;

    public ListAircraftModelsController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().aircraftModels());
    }

    public ListAircraftModelsController(final AuthorizationService authz,
                                        final AircraftModelRepository repo) {
        if (authz == null || repo == null) throw new IllegalArgumentException();
        this.authz = authz;
        this.repo = repo;
    }

    public Iterable<AircraftModel> allAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR,
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        return repo.findAll();
    }
}

