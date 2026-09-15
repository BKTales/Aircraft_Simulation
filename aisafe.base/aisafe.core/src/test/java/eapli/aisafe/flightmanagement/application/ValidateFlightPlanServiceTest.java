package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;
import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.*;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.ValidationStatus;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
import eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector;
import eapli.aisafe.routemanagement.domain.DeactivationDate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidateFlightPlanServiceTest {

    private final TestAircraftRepositoryFactory repos = new TestAircraftRepositoryFactory();
    private final InMemoryRouteRepository routes = new InMemoryRouteRepository();
    private final InMemoryAirportRepository airports = new InMemoryAirportRepository();
    private final AirControlAreaRepository areaRepository = mock(AirControlAreaRepository.class);
    private final IsolatedPilotUserRepository pilots = new IsolatedPilotUserRepository();
    private final FlightSimulationService simulationService = mock(FlightSimulationService.class);
    private CreateFlightPlanService createService;
    private ValidateFlightPlanService validateService;
    private PilotUser owner;
    private SystemUser ownerUser;
    private AirControlArea area;

    @BeforeEach
    void setUp() {
        area = lisboaArea();
        when(areaRepository.ofIdentity(any())).thenReturn(Optional.of(area));
        when(areaRepository.findAll()).thenReturn(List.of(area));
        final var engines = SimulatorJsonTestFixtures.newEngineRepository();
        SimulatorJsonTestFixtures.seedA320(repos.aircraftModels(), engines);
        createService = new CreateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), pilots, repos.aircraftModels(), engines);
        validateService = new ValidateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), airports, areaRepository, simulationService);
        airports.save(AIRPORT_OPO);
        airports.save(AIRPORT_LIS);
        routes.save(Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule()));
        repos.aircraft().save(sampleAircraft("CS-TP01"));
        final AircraftModel model = mock(AircraftModel.class);
        when(model.identity()).thenReturn(AircraftModelId.valueOf("A320"));
        owner = savePilot("pilot1", COMPANY_TP, model);
        ownerUser = owner.systemUser();
        assertTrue(createSampleFlight().isSuccess());
    }

    @Test
    void ensureDslFailureDoesNotInvokeSimulator() {
        final FlightDslParser parser = mock(FlightDslParser.class);
        when(parser.parse(anyString())).thenReturn(ParseResult.failure(List.of("line 1: syntax error")));
        final ValidateFlightPlanService service = new ValidateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), airports, areaRepository, parser, simulationService,
                new eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector());

        final ValidateFlightPlanResult result = service.validate("TP123", ownerUser);

        assertTrue(result.dslFailure());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        verify(simulationService, never()).simulateEligibleFlights(anyString(), any(), any(), anyList(), any());
    }

    @Test
    void ensureApprovedWhenSimulationPasses() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(passingResult());

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertTrue(result.passed());
        assertEquals(FlightPlanStatus.SIM_APPROVED, result.status());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.SIM_APPROVED, saved.flightPlan().status());
    }

    @Test
    @SuppressWarnings("unchecked")
    void ensureSimulatorReceivesOnlyTargetFlight() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(passingResult());

        validateService.validate("TP123", ownerUser);

        final ArgumentCaptor<List<Flight>> eligibleCaptor = ArgumentCaptor.forClass(List.class);
        verify(simulationService).simulateEligibleFlights(
                anyString(), any(), any(), eligibleCaptor.capture(), eq("TP123"));
        assertEquals(1, eligibleCaptor.getValue().size());
        assertEquals("TP123", eligibleCaptor.getValue().get(0).identity().toString());
    }

    @Test
    void ensureRejectedWhenSimulationFails() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(failingResult());

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.SIM_REJECTED, saved.flightPlan().status());
    }

    @Test
    void ensureRejectedWhenAggregatePassButTargetFlightFails() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(aggregatePassTargetLowAltitudeResult());

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        assertTrue(result.message().contains("minimum safe altitude"));
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.SIM_REJECTED, saved.flightPlan().status());
    }

    @Test
    void ensureOnlyOwnerCanValidate() {
        final SystemUser other = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("other@tap.pt", "password", "Other", "Pilot", "other@tap.pt")
                .withRoles(AISafeRoles.PILOT)
                .withUsername("other")
                .build();

        final ValidateFlightPlanResult result = validateService.validate("TP123", other);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertTrue(result.message().contains("owner pilot"));
    }

    @Test
    void ensureNonDraftPlanCannotBeValidated() {
        final Flight flight = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        flight.flightPlan().changeStatus(FlightPlanStatus.SIM_APPROVED);
        repos.flights().save(flight);

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertTrue(result.message().contains("DRAFT"));
    }

    @Test
    void ensureFlightNotFoundReturnsBlocked() {
        final ValidateFlightPlanResult result = validateService.validate("TP404", ownerUser);

        assertFalse(result.passed());
        assertTrue(result.message().contains("not found"));
    }

    // ---------- Preflight invariants ----------

    @Test
    void ensureBlockedWhenPlanHasNoDsl() {
        replaceDsl(flight("TP123"), null);

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertBlocked(result, "no stored DSL");
        verify(simulationService, never()).simulateEligibleFlights(anyString(), any(), any(), anyList(), any());
    }

    @Test
    void ensureBlockedWhenFlightHasNoPilot() {
        final Flight orphan = manualFlight("TP700", null, "CS-TP01", DEP, ARR);
        repos.flights().save(orphan);

        final ValidateFlightPlanResult result = validateService.validate("TP700", ownerUser);

        assertBlocked(result, "no pilot assigned");
    }

    // ---------- DSL gate with the real parser (AC1, AC5, AC6) ----------

    @Test
    void ensureRealParserSyntaxFailureReturnsErrorsAndDslContent() {
        final String invalidDsl = readResource("dsl/invalid_syntax.txt");
        replaceDsl(flight("TP123"), invalidDsl);

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertTrue(result.dslFailure());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertFalse(result.errors().isEmpty());
        assertEquals(invalidDsl, result.dslContent());
        verify(simulationService, never()).simulateEligibleFlights(anyString(), any(), any(), anyList(), any());
    }

    @Test
    void ensureRealParserSemanticFailureDoesNotInvokeSimulator() {
        replaceDsl(flight("TP123"), readResource("dsl/invalid_semantic_wind.txt"));

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertTrue(result.dslFailure());
        verify(simulationService, never()).simulateEligibleFlights(anyString(), any(), any(), anyList(), any());
    }

    // ---------- validateDslConsistency ----------

    @Test
    void ensureBlockedWhenDslFlightIdMismatch() {
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("WRONG", "OPO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP123", ownerUser), "does not match flight");
    }

    @Test
    void ensureBlockedWhenDslDepartureAirportMismatch() {
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP123", "FAO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP123", ownerUser), "departure airport");
    }

    @Test
    void ensureBlockedWhenDslArrivalAirportMismatch() {
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP123", "OPO", DSL_DEP, "FAO", DSL_ARR)));

        assertBlocked(service.validate("TP123", ownerUser), "arrival airport");
    }

    @Test
    void ensureBlockedWhenDslDepartureDiffersFromSchedule() {
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP123", "OPO", "2026-06-01 11:00", "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP123", ownerUser), "does not match flight schedule");
    }

    @Test
    void ensureBlockedWhenMultiLegAirportGap() {
        final FlightPlanDescriptor multiLeg = new FlightPlanDescriptor(
                "TP123", "CHARTER", 100, 8000.0, 500.0,
                List.of(
                        leg("OPO", DSL_DEP, "LIS", "2026-06-01 11:00"),
                        leg("FAO", "2026-06-01 11:30", "LIS", DSL_ARR)));
        final ValidateFlightPlanService service = serviceWithParser(parserReturning(multiLeg));

        assertBlocked(service.validate("TP123", ownerUser), "Multi-leg inconsistency");
    }

    // ---------- validateBusinessRules ----------

    @Test
    void ensureBlockedWhenFlightHasNoSchedule() {
        manualFlight("TP710", owner, "CS-TP01", null, null);
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(new FlightPlanDescriptor("TP710", "CHARTER", 100, 8000.0, 500.0, List.of())));

        assertBlocked(service.validate("TP710", ownerUser), "no schedule");
    }

    @Test
    void ensureBlockedWhenRouteInactive() {
        final Route route = routes.ofIdentity(ROUTE_NAME_TP123).orElseThrow();
        route.deactivate(DeactivationDate.valueOf(LocalDate.of(2026, 6, 1)));
        routes.save(route);
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP123", "OPO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP123", ownerUser), "inactive");
    }

    @Test
    void ensureBlockedWhenAircraftDecommissioned() {
        repos.aircraft().save(decommissionedAircraft("CS-DEC"));
        manualFlight("TP720", owner, "CS-DEC", DEP, ARR);
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP720", "OPO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP720", ownerUser), "decommissioned");
    }

    @Test
    void ensureBlockedWhenPilotBelongsToAnotherCompany() {
        final AirTransportCompany other = new AirTransportCompany(
                CompanyName.valueOf("Ryanair"), IATACode.valueOf("FR"), ICAOCode.valueOf("RYR"));
        final AircraftModel model = mock(AircraftModel.class);
        when(model.identity()).thenReturn(AircraftModelId.valueOf("A320"));
        final PilotUser foreign = savePilot("foreign", other, model);
        manualFlight("TP730", foreign, "CS-TP01", DEP, ARR);
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP730", "OPO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP730", foreign.systemUser()), "does not belong to the route's company");
    }

    @Test
    void ensureBlockedWhenPilotNotCertified() {
        final PilotUser uncertified = savePilotNoCert("nocert", COMPANY_TP);
        manualFlight("TP740", uncertified, "CS-TP01", DEP, ARR);
        final ValidateFlightPlanService service = serviceWithParser(
                parserReturning(descriptor("TP740", "OPO", DSL_DEP, "LIS", DSL_ARR)));

        assertBlocked(service.validate("TP740", uncertified.systemUser()), "not certified");
    }

    // ---------- Area / interval derivation ----------

    @Test
    void ensureSimulatorReceivesDerivedAreaAndInterval() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(passingResult());

        validateService.validate("TP123", ownerUser);

        final ArgumentCaptor<String> areaCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        final ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(simulationService).simulateEligibleFlights(
                areaCaptor.capture(), startCaptor.capture(), endCaptor.capture(), anyList(), eq("TP123"));
        assertEquals(area.identity().toString(), areaCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), startCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 6, 1, 12, 0), endCaptor.getValue());
    }

    @Test
    void ensureBlockedWhenNoAreaCrossed() {
        final RouteAreaCrossingDetector detector = mock(RouteAreaCrossingDetector.class);
        when(detector.crossesArea(any(), any())).thenReturn(false);
        final ValidateFlightPlanService service = new ValidateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), airports, areaRepository,
                new FlightDslParser(), simulationService, detector);

        assertBlocked(service.validate("TP123", ownerUser), "Could not derive air control area");
        verify(simulationService, never()).simulateEligibleFlights(anyString(), any(), any(), anyList(), any());
    }

    // ---------- Simulation outcomes ----------

    @Test
    void ensureRejectedWhenTargetRunsOutOfFuel() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(targetStatusResult(FlightExecutionStatus.OUT_OF_FUEL));

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        assertTrue(result.message().toLowerCase().contains("fuel"));
    }

    @Test
    void ensureRejectedWhenTargetMissingFromReport() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(aggregatePassEmptyFlightsResult());

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        assertTrue(result.message().contains("not found in simulation report"));
    }

    @Test
    void ensureRejectedAndPersistedWhenSimulatorThrows() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenThrow(new SimulatorExecutionException("boom", List.of("line1", "line2")));

        final ValidateFlightPlanResult result = validateService.validate("TP123", ownerUser);

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        assertEquals(List.of("line1", "line2"), result.simulationLog());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.SIM_REJECTED, saved.flightPlan().status());
    }

    @Test
    void ensureOnSimulationStartCallbackRunsBeforeSimulation() {
        when(simulationService.simulateEligibleFlights(anyString(), any(), any(), anyList(), any()))
                .thenReturn(passingResult());
        final boolean[] ran = {false};

        validateService.validate("TP123", ownerUser, () -> ran[0] = true);

        assertTrue(ran[0]);
    }

    // ---------- listValidatableForPilot ----------

    @Test
    void ensureListValidatableReturnsEmptyForNullPilot() {
        assertTrue(validateService.listValidatableForPilot(null).isEmpty());
    }

    @Test
    void ensureListValidatableReturnsOnlyOwnerDraftFlights() {
        final PilotUser otherPilot = savePilot("pilot2", COMPANY_TP, certifiedModel());
        manualFlight("TP800", otherPilot, "CS-TP01", DEP, ARR);

        final List<FlightValidationPreview> previews = validateService.listValidatableForPilot(ownerUser);

        assertTrue(previews.stream().anyMatch(p -> p.designator().equals("TP123")));
        assertFalse(previews.stream().anyMatch(p -> p.designator().equals("TP800")));
    }

    @Test
    void ensureListValidatableExcludesNonDraftPlans() {
        final Flight flight = flight("TP123");
        flight.flightPlan().changeStatus(FlightPlanStatus.SIM_APPROVED);
        repos.flights().save(flight);

        assertTrue(validateService.listValidatableForPilot(ownerUser).isEmpty());
    }

    @Test
    void ensureListValidatableSortsByScheduledDeparture() {
        manualFlight("TP810", owner, "CS-TP01",
                LocalDateTime.of(2026, 6, 5, 8, 0), LocalDateTime.of(2026, 6, 5, 10, 0));
        manualFlight("TP805", owner, "CS-TP01",
                LocalDateTime.of(2026, 6, 3, 8, 0), LocalDateTime.of(2026, 6, 3, 10, 0));

        final List<FlightValidationPreview> previews = validateService.listValidatableForPilot(ownerUser);
        final List<LocalDateTime> departures = previews.stream()
                .map(FlightValidationPreview::scheduledDeparture)
                .toList();

        for (int i = 1; i < departures.size(); i++) {
            assertTrue(!departures.get(i).isBefore(departures.get(i - 1)));
        }
    }

    // ---------- Helpers ----------

    private static final String DSL_DEP = "2026-06-01 10:00";
    private static final String DSL_ARR = "2026-06-01 12:00";
    private static final LocalDateTime DEP = LocalDateTime.of(2026, 6, 1, 10, 0);
    private static final LocalDateTime ARR = LocalDateTime.of(2026, 6, 1, 12, 0);

    private Flight flight(final String designator) {
        return repos.flights().findByDesignator(new FlightDesignator(designator)).orElseThrow();
    }

    private void replaceDsl(final Flight f, final String dsl) {
        final FlightPlan plan = f.flightPlan();
        plan.replaceDraftContent(plan.fuelLoad(), dsl, plan.jsonContent());
        repos.flights().save(f);
    }

    private Flight manualFlight(final String designator,
                                final PilotUser pilot,
                                final String registration,
                                final LocalDateTime departure,
                                final LocalDateTime arrival) {
        final FlightDesignator id = new FlightDesignator(designator);
        final Flight f = new Flight(id, ROUTE_NAME_TP123.toString(), registration);
        if (pilot != null) {
            f.assignPilotId(pilot);
        }
        if (departure != null && arrival != null) {
            f.assignSchedule(FlightSchedule.valueOf(departure, arrival));
        }
        f.assignFlightPlan(FlightPlan.forFlight(
                id, FlightPlanStatus.DRAFT, FuelLoad.valueOf(5000.0),
                "flight " + designator + " {}",
                SimulatorJsonTestFixtures.minimalSelfContainedJson("AREA-0")));
        repos.flights().save(f);
        return f;
    }

    private ValidateFlightPlanService serviceWithParser(final FlightDslParser parser) {
        return new ValidateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), airports, areaRepository,
                parser, simulationService, new RouteAreaCrossingDetector());
    }

    private static FlightDslParser parserReturning(final FlightPlanDescriptor descriptor) {
        final FlightDslParser parser = mock(FlightDslParser.class);
        when(parser.parse(anyString())).thenReturn(ParseResult.success(descriptor));
        return parser;
    }

    private static FlightPlanDescriptor descriptor(final String flightId,
                                                   final String depAirport,
                                                   final String depTime,
                                                   final String arrAirport,
                                                   final String arrTime) {
        return new FlightPlanDescriptor(flightId, "CHARTER", 100, 8000.0, 500.0,
                List.of(leg(depAirport, depTime, arrAirport, arrTime)));
    }

    private static LegDescriptor leg(final String depAirport,
                                     final String depTime,
                                     final String arrAirport,
                                     final String arrTime) {
        final RouteDescriptor route = new RouteDescriptor(List.of(new SegmentDescriptor(
                41.24, -8.68, 38.77, -9.13,
                List.of(new AltitudeSlotDescriptor(11000, 500)), 25, 10.0)));
        return new LegDescriptor(depAirport, depTime, arrAirport, arrTime, route, 12000.0, "kg");
    }

    private AircraftModel certifiedModel() {
        final AircraftModel model = mock(AircraftModel.class);
        when(model.identity()).thenReturn(AircraftModelId.valueOf("A320"));
        return model;
    }

    private PilotUser savePilotNoCert(final String username, final AirTransportCompany company) {
        final String email = username + "@tap.pt";
        final SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "Pilot", "One", email)
                .withRoles(AISafeRoles.PILOT)
                .withUsername(username)
                .build();
        user.activate();
        final PilotUser pilotUser = new PilotUser(
                user,
                SecurityClearance.valueOf(LocalDate.now().plusYears(1)),
                company,
                Phone.valueOf("+351900000000"),
                SkillsAssessment.valueOf(LocalDate.now().minusYears(1)));
        pilots.save(pilotUser);
        return pilotUser;
    }

    private static Aircraft decommissionedAircraft(final String registration) {
        return new Aircraft(
                AircraftRegistration.valueOf(registration),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine(),
                CabinConfiguration.ofEconomyBusinessFirst(180, 0, 0),
                new RegistrationCountry("PT"),
                IATACode.valueOf("TP"),
                OperationalStatus.DECOMMISSIONED,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - 5));
    }

    private static SimulationResult targetStatusResult(final FlightExecutionStatus status) {
        final FlightSimulationReport report = new FlightSimulationReport(
                eapli.aisafe.aircontrolarea.domain.AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(new FlightStatus(simulatorFlightId("TP123"), status)),
                List.of(),
                List.of());
        return new SimulationResult(report, 1, "/tmp/report.csv", "/tmp/plans");
    }

    private static SimulationResult aggregatePassEmptyFlightsResult() {
        final FlightSimulationReport report = new FlightSimulationReport(
                eapli.aisafe.aircontrolarea.domain.AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(),
                List.of(),
                List.of());
        return new SimulationResult(report, 1, "/tmp/report.csv", "/tmp/plans");
    }

    private static void assertBlocked(final ValidateFlightPlanResult result, final String messageContains) {
        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertTrue(result.message().contains(messageContains),
                "expected message to contain '" + messageContains + "' but was: " + result.message());
    }

    private static String readResource(final String classpath) {
        try {
            final var url = ValidateFlightPlanServiceTest.class.getClassLoader().getResource(classpath);
            assertNotNull(url, "Missing test resource: " + classpath);
            return Files.readString(Path.of(url.toURI()));
        } catch (final IOException | URISyntaxException e) {
            throw new UncheckedIOException(e instanceof IOException io ? io : new IOException(e));
        }
    }

    private CreateFlightPlanResult createSampleFlight() {
        return createService.createFlightPlan(new CreateFlightPlanRequest(
                "TP123",
                "CS-TP01",
                "pilot1",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                FuelQuantity.kilograms(5000),
                120,
                10000,
                500,
                Optional.empty()));
    }

    private static SimulationResult passingResult() {
        final FlightSimulationReport report = new FlightSimulationReport(
                eapli.aisafe.aircontrolarea.domain.AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(new FlightStatus(simulatorFlightId("TP123"), FlightExecutionStatus.SUCCESS)),
                List.of(),
                List.of());
        return new SimulationResult(report, 1, "/tmp/report.csv", "/tmp/plans");
    }

    private static SimulationResult aggregatePassTargetLowAltitudeResult() {
        final FlightSimulationReport report = new FlightSimulationReport(
                eapli.aisafe.aircontrolarea.domain.AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(new FlightStatus(simulatorFlightId("TP123"), FlightExecutionStatus.LOW_ALTITUDE)),
                List.of(),
                List.of());
        return new SimulationResult(report, 1, "/tmp/report.csv", "/tmp/plans");
    }

    private static String simulatorFlightId(final String designator) {
        long h = 1125899906842597L;
        for (int i = 0; i < designator.length(); i++) {
            h = 31 * h + designator.charAt(i);
        }
        return String.valueOf((int) (Math.abs(h) % Integer.MAX_VALUE));
    }

    private static SimulationResult failingResult() {
        final FlightSimulationReport report = new FlightSimulationReport(
                eapli.aisafe.aircontrolarea.domain.AreaCode.valueOf("AREA-0"),
                ValidationStatus.FAIL,
                List.of(),
                List.of(),
                List.of());
        return new SimulationResult(report, 1, "/tmp/report.csv", "/tmp/plans");
    }

    private static Aircraft sampleAircraft(final String registration) {
        return new Aircraft(
                AircraftRegistration.valueOf(registration),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine(),
                CabinConfiguration.ofEconomyBusinessFirst(180, 0, 0),
                new RegistrationCountry("PT"),
                IATACode.valueOf("TP"),
                OperationalStatus.ACTIVE,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - 5));
    }

    private PilotUser savePilot(final String username,
                                final AirTransportCompany company,
                                final AircraftModel aircraftModel) {
        final String email = username + "@tap.pt";
        final SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "Pilot", "One", email)
                .withRoles(AISafeRoles.PILOT)
                .withUsername(username)
                .build();
        user.activate();
        final PilotUser pilotUser = new PilotUser(
                user,
                SecurityClearance.valueOf(LocalDate.now().plusYears(1)),
                company,
                Phone.valueOf("+351900000000"),
                SkillsAssessment.valueOf(LocalDate.now().minusYears(1)));
        pilotUser.addPilotCertification(new PilotCertification(
                aircraftModel, DueDate.valueOf(LocalDate.now().minusYears(1), LocalDate.now().plusYears(1))));
        pilots.save(pilotUser);
        return pilotUser;
    }

    private static final class InMemoryRouteRepository
            extends InMemoryDomainRepository<Route, RouteName>
            implements RouteRepository {
    }

    private static AirControlArea lisboaArea() {
        return new AirControlArea(
                new AirControlAreaName("TMA Lisboa"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(32.0f, -17.5f),
                        GeographicCoords.valueOf(42.5f, -17.5f),
                        GeographicCoords.valueOf(42.5f, -6.0f),
                        GeographicCoords.valueOf(32.0f, -6.0f))),
                MinFuelRequirement.valueOf(2000f));
    }

    private static final class IsolatedPilotUserRepository
            extends InMemoryDomainRepository<PilotUser, eapli.aisafe.usermanagement.domain.AISafeUserId>
            implements eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository {

        @Override
        public Iterable<PilotUser> findPilotByCompanyAndActive(final AirTransportCompany company) {
            return List.of();
        }

        @Override
        public Optional<PilotUser> findByUsername(final eapli.framework.infrastructure.authz.domain.model.Username username) {
            return java.util.stream.StreamSupport.stream(findAll().spliterator(), false)
                    .filter(p -> p.systemUser().username().equals(username))
                    .findFirst();
        }

        @Override
        public Optional<PilotUser> findByEmail(final eapli.framework.general.domain.model.EmailAddress email) {
            return Optional.empty();
        }

        @Override
        public Optional<PilotUser> findByEmailWithLock(final eapli.framework.general.domain.model.EmailAddress email,
                                                       final eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany company) {
            return Optional.empty();
        }
    }
}
