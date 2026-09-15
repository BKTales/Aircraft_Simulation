package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class ListFleetController {

    private final AuthorizationService authz;
    private final AircraftService aircraftService;
    private final CompanyCollaboratorUserRepository collaborators;

    public ListFleetController() {
        this(AuthzRegistry.authorizationService(),
                new AircraftService(
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().aircraftModels()),
                PersistenceContext.repositories().collaborators());
    }

    public ListFleetController(final AuthorizationService authz,
                               final AircraftService aircraftService,
                               final CompanyCollaboratorUserRepository collaborators) {
        if (authz == null || aircraftService == null || collaborators == null) {
            throw new IllegalArgumentException("Authorization, service and collaborator repository are required.");
        }
        this.authz = authz;
        this.aircraftService = aircraftService;
        this.collaborators = collaborators;
    }

    public Iterable<Aircraft> listFleet(final FleetListCriteria criteria) {
        final var collaborator = CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);
        return aircraftService.listFleet(collaborator.airTransportCompany().identity(), criteria);
    }

    public Iterable<AircraftModel> modelsUsedInCompanyFleet() {
        final var collaborator = CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);
        return aircraftService.modelsUsedInFleet(collaborator.airTransportCompany().identity());
    }

    public Iterable<ManufacturerId> manufacturersUsedInCompanyFleet() {
        final var collaborator = CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);
        return aircraftService.manufacturersUsedInFleet(collaborator.airTransportCompany().identity());
    }
}
