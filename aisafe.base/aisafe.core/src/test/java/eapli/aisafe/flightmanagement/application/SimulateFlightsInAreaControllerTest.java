package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.flightmanagement.application.exceptions.AirControlAreaNotFoundException;
import eapli.aisafe.flightmanagement.application.exceptions.NoEligibleFlightsException;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.ValidationStatus;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimulateFlightsInAreaControllerTest {

    private AuthorizationService authz;
    private AirControlAreaService areaService;
    private AirControlAreaRepository areaRepository;
    private FlightSimulationService simulationService;
    private SimulateFlightsInAreaController controller;

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        areaService = mock(AirControlAreaService.class);
        areaRepository = mock(AirControlAreaRepository.class);
        simulationService = mock(FlightSimulationService.class);
        controller = new SimulateFlightsInAreaController(authz, areaService, areaRepository, simulationService);
    }

    @Test
    void ensureAvailableAreasRequiresFcoRole() {
        final AirControlArea area = sampleArea();
        when(areaService.availableAreas()).thenReturn(List.of(area));

        final Iterable<AirControlArea> areas = controller.availableAreas();

        assertNotNull(areas);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
        verify(areaService).availableAreas();
    }

    @Test
    void ensureSimulateThrowsWhenAreaMissing() {
        when(areaRepository.ofIdentity(any(AreaCode.class))).thenReturn(Optional.empty());

        assertThrows(AirControlAreaNotFoundException.class, () -> controller.simulateFlightsInArea(
                "AREA-9",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 2, 0, 0)
        ));
    }

    @Test
    void ensureSimulateDelegatesToService() throws Exception {
        final AreaCode code = AreaCode.valueOf("AREA-0");
        when(areaRepository.ofIdentity(code)).thenReturn(Optional.of(sampleArea()));

        final FlightSimulationReport report = mock(FlightSimulationReport.class);
        when(report.passed()).thenReturn(true);
        final SimulationResult expected = new SimulationResult(report, 2, "/tmp/report.csv", "/tmp/plans");
        when(simulationService.simulateFlightsInArea(
                eq("AREA-0"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(expected);

        final SimulationResult result = controller.simulateFlightsInArea(
                "AREA-0",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 18, 0)
        );

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
    }

    @Test
    void ensureConstructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new SimulateFlightsInAreaController(null, areaService, areaRepository, simulationService));
        assertThrows(IllegalArgumentException.class, () -> new SimulateFlightsInAreaController(authz, null, areaRepository, simulationService));
        assertThrows(IllegalArgumentException.class, () -> new SimulateFlightsInAreaController(authz, areaService, null, simulationService));
        assertThrows(IllegalArgumentException.class, () -> new SimulateFlightsInAreaController(authz, areaService, areaRepository, null));
    }

    private static AirControlArea sampleArea() {
        return new AirControlArea(
                new AirControlAreaName("Test"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(0f, 0f),
                        GeographicCoords.valueOf(1f, 0f),
                        GeographicCoords.valueOf(0f, 1f)
                )),
                MinFuelRequirement.valueOf(100f)
        );
    }
}
