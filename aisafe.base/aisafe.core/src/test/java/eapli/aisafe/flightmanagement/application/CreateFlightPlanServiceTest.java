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
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.*;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static eapli.aisafe.routemanagement.RouteTestFixtures.mondaySchedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateFlightPlanServiceTest {

    private final TestAircraftRepositoryFactory repos = new TestAircraftRepositoryFactory();
    private final IsolatedRouteRepository routes = new IsolatedRouteRepository();
    private final IsolatedPilotUserRepository pilots = new IsolatedPilotUserRepository();
    private final SimulatorJsonTestFixtures.InMemoryEngineModelRepository engines =
            SimulatorJsonTestFixtures.newEngineRepository();
    private CreateFlightPlanService service;
    private PilotUser pilot;
    private AircraftModel model;

    @BeforeEach
    void setUp() {
        SimulatorJsonTestFixtures.seedA320(repos.aircraftModels(), engines);
        service = new CreateFlightPlanService(
                repos.flights(), routes, repos.aircraft(), pilots, repos.aircraftModels(), engines);
        model = repos.aircraftModels().ofIdentity(SimulatorJsonTestFixtures.MODEL_ID).orElseThrow();
        pilot = savePilot("pilot1", COMPANY_TP, model);
        repos.aircraft().save(sampleAircraft("CS-TP01"));
        routes.save(Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule()));
    }

    @Test
    void createsDraftFlightForCharterRoute() {
        final CreateFlightPlanResult result = createSampleFlight();
        assertTrue(result.isSuccess());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.DRAFT, saved.flightPlan().status());
        assertEquals(ROUTE_NAME_TP123.toString(), saved.routeName());
        assertTrue(saved.flightPlan().hasJsonContent());
        assertTrue(saved.flightPlan().jsonContent().contains("\"Aircraft\""));
        assertTrue(saved.flightPlan().jsonContent().contains("\"DepartureAirport\""));
        assertTrue(saved.flightPlan().hasDslContent());
        assertTrue(saved.flightPlan().dslContent().contains("flight TP123"));
        assertNull(saved.flightLoad());
        assertEquals("pilot1", saved.pilot().systemUser().username().toString());
    }

    private CreateFlightPlanResult createSampleFlight() {
        return service.createFlightPlan(new CreateFlightPlanRequest(
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

    @Test
    void replacesPlanSilentlyWhenExistingPlanIsDraft() {
        assertTrue(createSampleFlight().isSuccess());
        final CreateFlightPlanResult second = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0),
                FuelQuantity.kilograms(4000), 0, 0, 0, Optional.empty()));

        assertTrue(second.isSuccess());
        assertTrue(second.replaced());
        assertEquals(FlightPlanStatus.DRAFT, second.replacedFromStatus().orElseThrow());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        assertEquals(FlightPlanStatus.DRAFT, saved.flightPlan().status());
        assertEquals(4000.0, saved.flightPlan().fuelLoad().quantityKg(), 0.01);
        assertTrue(saved.flightPlan().dslContent().contains("4000"));
    }

    @Test
    void replacesPlanSilentlyWhenExistingPlanIsSimRejected() {
        assertTrue(createSampleFlight().isSuccess());
        final Flight existing = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        existing.transitionFlightPlanStatus(FlightPlanStatus.SIM_REJECTED);
        repos.flights().save(existing);

        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0),
                FuelQuantity.kilograms(3500), 0, 0, 0, Optional.empty()));

        assertTrue(result.isSuccess());
        assertTrue(result.replaced());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.replacedFromStatus().orElseThrow());
        assertEquals(FlightPlanStatus.DRAFT,
                repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow().flightPlan().status());
    }

    @Test
    void needsConfirmationWhenExistingPlanIsSimApproved() {
        assertTrue(createSampleFlight().isSuccess());
        final Flight existing = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        existing.transitionFlightPlanStatus(FlightPlanStatus.SIM_APPROVED);
        repos.flights().save(existing);

        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0),
                FuelQuantity.kilograms(4000), 0, 0, 0, Optional.empty()));

        assertTrue(result.needsConfirmation());
        assertEquals(FlightPlanStatus.SIM_APPROVED,
                repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow().flightPlan().status());
    }

    @Test
    void replacesPlanWhenSimApprovedAndUserConfirms() {
        assertTrue(createSampleFlight().isSuccess());
        final Flight existing = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        existing.transitionFlightPlanStatus(FlightPlanStatus.SIM_APPROVED);
        repos.flights().save(existing);

        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0),
                FuelQuantity.kilograms(4000), 0, 0, 0, Optional.empty(), true));

        assertTrue(result.isSuccess());
        assertTrue(result.replaced());
        assertEquals(FlightPlanStatus.SIM_APPROVED, result.replacedFromStatus().orElseThrow());
        assertEquals(FlightPlanStatus.DRAFT,
                repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow().flightPlan().status());
    }

    @Test
    void needsConfirmationWhenExistingPlanIsSubmittedForSimulation() {
        assertTrue(createSampleFlight().isSuccess());
        final Flight existing = repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow();
        existing.transitionFlightPlanStatus(FlightPlanStatus.SUBMITTED_FOR_SIMULATION);
        repos.flights().save(existing);

        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0),
                FuelQuantity.kilograms(4000), 0, 0, 0, Optional.empty()));

        assertTrue(result.needsConfirmation());
        assertEquals(FlightPlanStatus.SUBMITTED_FOR_SIMULATION,
                repos.flights().findByDesignator(new FlightDesignator("TP123")).orElseThrow().flightPlan().status());
    }

    @Test
    void listSelectableRoutes_returnsActiveRoutesForSessionPilotCompany() {
        final List<Route> result = service.listSelectableRoutes(
                Username.valueOf("pilot1"), LocalDate.of(2026, 6, 1));

        assertEquals(1, result.size());
        assertEquals(ROUTE_NAME_TP123, result.get(0).identity());
        assertEquals(ROUTE_NAME_TP123.toString(), result.get(0).identity().toString());
    }

    @Test
    void listSelectableRoutes_excludesDeactivatedRouteOnDate() {
        routes.save(Route.regularRouteDeactivated(
                RouteName.valueOf("TP888"), COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS,
                mondaySchedule(), LocalDate.of(2026, 6, 1)));

        final List<Route> result = service.listSelectableRoutes(
                Username.valueOf("pilot1"), LocalDate.of(2026, 6, 1));

        assertEquals(1, result.size());
        assertEquals(ROUTE_NAME_TP123, result.get(0).identity());
    }

    @Test
    void listCompanyActiveAircraftRegistrations_returnsSortedRegistrations() {
        repos.aircraft().save(sampleAircraft("CS-TP02"));

        final List<String> result = service.listCompanyActiveAircraftRegistrations(Username.valueOf("pilot1"));

        assertEquals(List.of("CS-TP01", "CS-TP02"), result);
    }

    @Test
    void listCompanyPilots_returnsActivePilotsForCompany() {
        savePilot("pilot2", COMPANY_TP, model);

        final List<PilotUser> result = service.listCompanyPilots(Username.valueOf("pilot1"));

        assertEquals(2, result.size());
        assertEquals("pilot1", result.get(0).systemUser().username().toString());
        assertEquals("pilot2", result.get(1).systemUser().username().toString());
    }

    @Test
    void listMethods_throwWhenSessionPilotNotFound() {
        final Username unknown = Username.valueOf("unknown");

        assertThrows(IllegalStateException.class,
                () -> service.listSelectableRoutes(unknown, LocalDate.of(2026, 6, 1)));
        assertThrows(IllegalStateException.class,
                () -> service.listCompanyActiveAircraftRegistrations(unknown));
        assertThrows(IllegalStateException.class,
                () -> service.listCompanyPilots(unknown));
    }

    @Test
    void failsWhenArrivalNotAfterDeparture() {
        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP123", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 1, 12, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                FuelQuantity.kilograms(5000), 0, 0, 0, Optional.empty()));

        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().toLowerCase().contains("arrival"));
    }

    @Test
    void failsWhenRouteDeactivated() {
        routes.save(Route.regularRouteDeactivated(
                RouteName.valueOf("TP888"), COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS,
                mondaySchedule(), LocalDate.of(2026, 6, 1)));

        final CreateFlightPlanResult result = service.createFlightPlan(new CreateFlightPlanRequest(
                "TP888", "CS-TP01", "pilot1",
                LocalDateTime.of(2026, 6, 2, 10, 0),
                LocalDateTime.of(2026, 6, 2, 12, 0),
                FuelQuantity.kilograms(3000), 0, 0, 0, Optional.empty()));

        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().toLowerCase().contains("deactivated"));
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
                .withRoles(eapli.aisafe.usermanagement.domain.AISafeRoles.PILOT)
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

    private static final class IsolatedRouteRepository
            extends InMemoryDomainRepository<Route, RouteName>
            implements RouteRepository {

        private final List<Route> savedRoutes = new ArrayList<>();

        @Override
        public Route save(final Route entity) {
            savedRoutes.add(entity);
            return super.save(entity);
        }

        @Override
        public Iterable<Route> findActiveByCompany(final IATACode companyIATACode, final LocalDate asOf) {
            return savedRoutes.stream()
                    .filter(route -> route.companyIATACode().equals(companyIATACode))
                    .filter(route -> route.isActiveOn(asOf))
                    .toList();
        }
    }

    private static final class IsolatedPilotUserRepository
            extends InMemoryDomainRepository<PilotUser, eapli.aisafe.usermanagement.domain.AISafeUserId>
            implements eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository {

        private final java.util.List<PilotUser> savedPilots = new java.util.ArrayList<>();

        @Override
        public PilotUser save(final PilotUser entity) {
            savedPilots.add(entity);
            return super.save(entity);
        }

        @Override
        public Iterable<PilotUser> findPilotByCompanyAndActive(final AirTransportCompany company) {
            return savedPilots.stream()
                    .filter(p -> p.airTransportCompany().identity().equals(company.identity()))
                    .filter(p -> p.systemUser().isActive())
                    .toList();
        }

        @Override
        public Optional<PilotUser> findByUsername(final eapli.framework.infrastructure.authz.domain.model.Username username) {
            return savedPilots.stream()
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
