package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class AddEngineModelToAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelService service;

    public AddEngineModelToAircraftModelController() {
        this(AuthzRegistry.authorizationService(),
                new AircraftModelService(
                        PersistenceContext.repositories().aircraftModels(),
                        PersistenceContext.repositories().manufacturers(),
                        PersistenceContext.repositories().engineModels()
                ));
    }

    public AddEngineModelToAircraftModelController(final AuthorizationService authz,
                                                    final AircraftModelService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and aircraft model service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public AircraftModel addEngineModelToAircraftModel(final String aircraftModelId,
                                                        final String engineModelId) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        return service.addEngineModelToAircraftModel(aircraftModelId, engineModelId);
    }
}
