package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDateTime;

@UseCaseController
public class DecommissionAircraftController {

    private final AuthorizationService authz;
    private final DecommissionAircraftService service;
    private final CompanyCollaboratorUserRepository collaborators;

    public DecommissionAircraftController() {
        this(AuthzRegistry.authorizationService(),
                new DecommissionAircraftService(
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().flights()),
                PersistenceContext.repositories().collaborators());
    }

    public DecommissionAircraftController(final AuthorizationService authz,
                                          final DecommissionAircraftService service,
                                          final CompanyCollaboratorUserRepository collaborators) {
        if (authz == null || service == null || collaborators == null) {
            throw new IllegalArgumentException("Authorization, service and collaborator repository are required.");
        }
        this.authz = authz;
        this.service = service;
        this.collaborators = collaborators;
    }

    public Aircraft decommissionAircraft(final String registration) {
        final CompanyCollaboratorUser collaborator =
                CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);
        return service.decommission(registration, collaborator.airTransportCompany().identity(), LocalDateTime.now());
    }

    public Iterable<Aircraft> listActiveCompanyAircraft() {
        final CompanyCollaboratorUser collaborator =
                CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators);
        return service.listActiveFleet(collaborator.airTransportCompany().identity());
    }
}
