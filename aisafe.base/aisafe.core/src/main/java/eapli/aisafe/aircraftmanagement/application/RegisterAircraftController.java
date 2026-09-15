package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAircraftController {

    private final AuthorizationService authz;
    private final AircraftService service;
    private final CompanyCollaboratorUserRepository collaborators;

    public RegisterAircraftController() {
        this(AuthzRegistry.authorizationService(),
                new AircraftService(
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().aircraftModels()),
                PersistenceContext.repositories().collaborators());
    }

    public RegisterAircraftController(final AuthorizationService authz,
                                      final AircraftService service,
                                      final CompanyCollaboratorUserRepository collaborators) {
        if (authz == null || service == null || collaborators == null) {
            throw new IllegalArgumentException("Authorization, aircraft service and collaborator repository are required.");
        }
        this.authz = authz;
        this.service = service;
        this.collaborators = collaborators;
    }

    public Aircraft registerAircraft(final String registration,
                                     final String modelId,
                                     final String engineModelId,
                                     final int economySeats,
                                     final int businessSeats,
                                     final int firstClassSeats,
                                     final String registrationCountryIso2,
                                     final int flightCrewCount,
                                     final int yearOfManufacture) {
        final CompanyCollaboratorUser collaborator =
                CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);

        final CabinConfiguration cabin =
                CabinConfiguration.ofEconomyBusinessFirst(economySeats, businessSeats, firstClassSeats);

        return service.registerAircraft(registration, modelId, engineModelId, cabin,
                registrationCountryIso2, flightCrewCount, yearOfManufacture,
                collaborator.airTransportCompany().identity());
    }
}
