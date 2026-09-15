package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.companycollaboratormanagment.application.PilotCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.List;

@UseCaseController
public final class ValidateFlightPlanController {

    private final AuthorizationService authz;
    private final ValidateFlightPlanService service;
    private final PilotUserRepository pilots;
    private final PilotCollaboratorUserService pilotService;

    public ValidateFlightPlanController() {
        this(AuthzRegistry.authorizationService(),
                new ValidateFlightPlanService(
                        PersistenceContext.repositories().flights(),
                        PersistenceContext.repositories().routes(),
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().airports(),
                        PersistenceContext.repositories().airControlArea(),
                        new FlightSimulationService(
                                PersistenceContext.repositories().flights(),
                                PersistenceContext.repositories().airControlArea(),
                                PersistenceContext.repositories().aircraft(),
                                PersistenceContext.repositories().aircraftModels(),
                                PersistenceContext.repositories().engineModels(),
                                PersistenceContext.repositories().airports(),
                                PersistenceContext.repositories().weatherData())),
                PersistenceContext.repositories().pilots(),
                new PilotCollaboratorUserService());
    }

    ValidateFlightPlanController(final AuthorizationService authz,
                                 final ValidateFlightPlanService service,
                                 final PilotUserRepository pilots,
                                 final PilotCollaboratorUserService pilotService) {
        if (authz == null || service == null || pilots == null || pilotService == null) {
            throw new IllegalArgumentException("Dependencies are required.");
        }
        this.authz = authz;
        this.service = service;
        this.pilots = pilots;
        this.pilotService = pilotService;
    }

    /** DRAFT flights owned by the authenticated pilot (US085 candidates). */
    public List<FlightValidationPreview> listValidatableFlights() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        final SystemUser user = authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session."))
                .authenticatedUser();
        pilotService.findPilot(pilots, user.username());
        return service.listValidatableForPilot(user);
    }

    public ValidateFlightPlanResult validateFlightPlan(final String flightDesignator) {
        return validateFlightPlan(flightDesignator, null);
    }

    public ValidateFlightPlanResult validateFlightPlan(final String flightDesignator,
                                                       final Runnable onSimulationStart) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        final SystemUser user = authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session."))
                .authenticatedUser();
        pilotService.findPilot(pilots, user.username());
        return service.validate(flightDesignator, user, onSimulationStart);
    }
}
