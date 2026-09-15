package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class ListEngineModelsController {

    private final AuthorizationService authz;
    private final EngineModelRepository repo;

    public ListEngineModelsController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().engineModels());
    }

    public ListEngineModelsController(final AuthorizationService authz,
                                      final EngineModelRepository repo) {
        if (authz == null || repo == null) throw new IllegalArgumentException();
        this.authz = authz;
        this.repo = repo;
    }

    public Iterable<EngineModel> allEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        return repo.findAll();
    }
}

