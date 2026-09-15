package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class CreateEngineModelController {

    private final AuthorizationService authz;
    private final EngineModelService service;

    public CreateEngineModelController() {
        this(AuthzRegistry.authorizationService(),
                new EngineModelService(
                        PersistenceContext.repositories().engineModels(),
                        PersistenceContext.repositories().manufacturers()
                ));
    }

    public CreateEngineModelController(final AuthorizationService authz,
                                       final EngineModelService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and engine model service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public EngineModel createEngineModel(final String name,
                                         final String manufacturerId,
                                         final String motorization,
                                         final double thrustAtStatic,
                                         final double thrustAtCruise,
                                         final String fuelType,
                                         final double tsfc) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        return service.createEngineModel(name, manufacturerId, motorization, thrustAtStatic, thrustAtCruise, fuelType, tsfc);
    }
}

