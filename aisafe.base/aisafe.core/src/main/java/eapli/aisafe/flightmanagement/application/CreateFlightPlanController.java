package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Username;

import java.time.LocalDate;
import java.util.List;

@UseCaseController
public final class CreateFlightPlanController {

    private final AuthorizationService authz;
    private final CreateFlightPlanService service;

    public CreateFlightPlanController() {
        this(AuthzRegistry.authorizationService(),
                new CreateFlightPlanService(
                        PersistenceContext.repositories().flights(),
                        PersistenceContext.repositories().routes(),
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().pilots(),
                        PersistenceContext.repositories().aircraftModels(),
                        PersistenceContext.repositories().engineModels()));
    }

    CreateFlightPlanController(final AuthorizationService authz,
                               final CreateFlightPlanService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and create flight plan service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public List<Route> listSelectableRoutes(final LocalDate asOf) {
        ensurePilot();
        return service.listSelectableRoutes(sessionUsername(), asOf);
    }

    public List<String> listCompanyActiveAircraftRegistrations() {
        ensurePilot();
        return service.listCompanyActiveAircraftRegistrations(sessionUsername());
    }

    public List<PilotUser> listCompanyPilots() {
        ensurePilot();
        return service.listCompanyPilots(sessionUsername());
    }

    public CreateFlightPlanResult createFlightPlan(final CreateFlightPlanRequest request) {
        ensurePilot();
        return service.createFlightPlan(request);
    }

    private Username sessionUsername() {
        return authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session."))
                .authenticatedUser()
                .username();
    }

    private void ensurePilot() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
    }
}
