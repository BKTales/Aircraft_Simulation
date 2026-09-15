package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.companycollaboratormanagment.application.CompanyCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.application.PilotCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.flightmanagement.application.importfile.FlightPlanFileImportResult;
import eapli.aisafe.flightmanagement.application.importfile.FlightPlanFileImportStrategyFactory;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.nio.file.Path;
import java.util.List;
@UseCaseController
public final class ImportFlightPlanFromFileController {

    private final AuthorizationService authz;
    private final FlightPlanFileImportStrategyFactory importFactory;
    private final ImportFlightPlanService importService;
    private final PilotCollaboratorUserService pilotService;
    private final PilotUserRepository pilotUserRepository;

    public ImportFlightPlanFromFileController() {
        this(AuthzRegistry.authorizationService(),
                new FlightPlanFileImportStrategyFactory(),
                new ImportFlightPlanService(
                        PersistenceContext.repositories().flights(),
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().aircraftModels(),
                        PersistenceContext.repositories().engineModels(),
                        PersistenceContext.repositories().airports()),
                new PilotCollaboratorUserService(),
                PersistenceContext.repositories().pilots());
    }

    ImportFlightPlanFromFileController(final AuthorizationService authz,
                                       final FlightPlanFileImportStrategyFactory importFactory,
                                       final ImportFlightPlanService importService,
                                       final PilotCollaboratorUserService pilotService,
                                       final PilotUserRepository pilotUserRepository) {
        if (authz == null || importFactory == null || importService == null) {
            throw new IllegalArgumentException("Dependencies are required.");
        }
        this.authz = authz;
        this.importFactory = importFactory;
        this.importService = importService;
        this.pilotService = pilotService;
        this.pilotUserRepository = pilotUserRepository;
    }

    public ParseResult parseFile(final Path path) {
        ensurePilot();
        return importFactory.parse(path).parseResult();
    }

    public List<String> activeAircraftRegistrations() {
        ensurePilot();
        final SystemUser user = authz.session().orElseThrow().authenticatedUser();
        final PilotUser pilotUser = pilotService.findPilot(pilotUserRepository, user.username());
        return importService.listActiveAircraftRegistrations(pilotUser.airTransportCompany().identity());
    }

    public ImportFlightPlanResult importValidPlan(final Path path, final String aircraftRegistration) {
        ensurePilot();
        final FlightPlanFileImportResult fileResult = importFactory.parse(path);
        if (!fileResult.isValid()) {
            final String message = fileResult.errors().isEmpty()
                    ? "Flight plan is invalid."
                    : String.join("; ", fileResult.errors());
            return ImportFlightPlanResult.failure(message);
        }

        final SystemUser user = authz.session()
                .orElseThrow()
                .authenticatedUser();
        final PilotUser pilotUser = pilotService.findPilot(
                pilotUserRepository, user.username());
        return importService.importFlightPlan(
                fileResult.descriptor(),
                fileResult.canonicalDslContent(),
                pilotUser,
                aircraftRegistration);
    }

    private void ensurePilot() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
    }
}
