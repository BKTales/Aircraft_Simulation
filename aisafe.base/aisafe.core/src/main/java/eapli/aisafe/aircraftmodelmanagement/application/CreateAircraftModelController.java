package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

@UseCaseController
public class CreateAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelService service;

    public CreateAircraftModelController() {
        this(AuthzRegistry.authorizationService(),
                new AircraftModelService(
                        PersistenceContext.repositories().aircraftModels(),
                        PersistenceContext.repositories().manufacturers(),
                        PersistenceContext.repositories().engineModels()
                ));
    }

    public CreateAircraftModelController(final AuthorizationService authz,
                                         final AircraftModelService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and aircraft model service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public AircraftModel createAircraftModel(final String modelId,
                                             final String modelName,
                                             final AircraftType aircraftType,
                                             final String manufacturerId,
                                             final double mtow,
                                             final double mzfw,
                                             final double emptyWeight,
                                             final double wingArea,
                                             final double wingSpan,
                                             final double cd0,
                                             final double cl,
                                             final double serviceCeiling,
                                             final double cruiseSpeed,
                                             final double fuelCapacity,
                                             final double maxRange,
                                             final int maxPassengerSeats,
                                             final int numberOfEngines,
                                             final List<String> engineModelIds) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        return service.createAircraftModel(modelId, modelName, aircraftType, manufacturerId,
                mtow, mzfw, emptyWeight, wingArea, wingSpan, cd0, cl,
                serviceCeiling, cruiseSpeed, fuelCapacity, maxRange,
                maxPassengerSeats, numberOfEngines, engineModelIds);
    }
}
