package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.util.List;

@UseCaseController
public class DeactivateRouteController {

    private final AuthorizationService authz;
    private final DeactivateRouteService service;
    private final CompanyCollaboratorUserRepository collaborators;

    public DeactivateRouteController() {
        this(AuthzRegistry.authorizationService(),
                new DeactivateRouteService(
                        PersistenceContext.repositories().routes(),
                        PersistenceContext.repositories().flights()),
                PersistenceContext.repositories().collaborators());
    }

    public DeactivateRouteController(final AuthorizationService authz,
                                     final DeactivateRouteService service,
                                     final CompanyCollaboratorUserRepository collaborators) {
        if (authz == null || service == null || collaborators == null) {
            throw new IllegalArgumentException(
                    "Authorization service, deactivate route service and collaborator repository are required.");
        }
        this.authz = authz;
        this.service = service;
        this.collaborators = collaborators;
    }

    public List<Route> listActiveRoutes() {
        return service.listActiveRoutesByCompany(requireCompany());
    }

    public List<ActiveRouteOption> listActiveRouteOptions() {
        return service.listActiveRouteOptionsByCompany(requireCompany());
    }

    public Route deactivateRoute(final String routeName, final LocalDate deactivationDate) {
        return service.deactivateRoute(routeName, deactivationDate, requireCompany());
    }

    private AirTransportCompany requireCompany() {
        return CompanyRouteCollaboratorSession
                .requireAirTransportCompanyCollaborator(authz, collaborators)
                .airTransportCompany();
    }
}
