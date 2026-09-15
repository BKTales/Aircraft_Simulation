package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.flightmanagement.application.exceptions.AirControlAreaNotFoundException;
import eapli.aisafe.flightmanagement.application.exceptions.NoEligibleFlightsException;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDateTime;
import java.util.List;

@UseCaseController
public final class SimulateFlightsInAreaController {

    private final AuthorizationService authz;
    private final AirControlAreaService areaService;
    private final AirControlAreaRepository areaRepository;
    private final FlightSimulationService simulationService;

    public SimulateFlightsInAreaController() {
        this(AuthzRegistry.authorizationService(),
                new AirControlAreaService(),
                PersistenceContext.repositories().airControlArea(),
                new FlightSimulationService(
                        PersistenceContext.repositories().flights(),
                        PersistenceContext.repositories().airControlArea(),
                        PersistenceContext.repositories().aircraft(),
                        PersistenceContext.repositories().aircraftModels(),
                        PersistenceContext.repositories().engineModels(),
                        PersistenceContext.repositories().airports(),
                        PersistenceContext.repositories().weatherData()));
    }

    SimulateFlightsInAreaController(final AuthorizationService authz,
                                    final AirControlAreaService areaService,
                                    final AirControlAreaRepository areaRepository,
                                    final FlightSimulationService simulationService) {
        if (authz == null || areaService == null || areaRepository == null || simulationService == null) {
            throw new IllegalArgumentException("Services are required.");
        }
        this.authz = authz;
        this.areaService = areaService;
        this.areaRepository = areaRepository;
        this.simulationService = simulationService;
    }

    public Iterable<AirControlArea> availableAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
        return areaService.availableAreas();
    }

    public List<EligibleFlightPreview> previewEligibleFlights(final String areaCode,
                                                              final LocalDateTime intervalStart,
                                                              final LocalDateTime intervalEnd)
            throws AirControlAreaNotFoundException {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
        areaRepository.ofIdentity(AreaCode.valueOf(areaCode))
                .orElseThrow(() -> new AirControlAreaNotFoundException(areaCode));
        return simulationService.listEligibleForArea(areaCode, intervalStart, intervalEnd);
    }

    public SimulationResult simulateFlightsInArea(final String areaCode,
                                                    final LocalDateTime intervalStart,
                                                    final LocalDateTime intervalEnd)
            throws AirControlAreaNotFoundException, NoEligibleFlightsException, SimulatorExecutionException {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);

        areaRepository.ofIdentity(AreaCode.valueOf(areaCode))
                .orElseThrow(() -> new AirControlAreaNotFoundException(areaCode));

        return simulationService.simulateFlightsInArea(areaCode, intervalStart, intervalEnd);
    }
}
