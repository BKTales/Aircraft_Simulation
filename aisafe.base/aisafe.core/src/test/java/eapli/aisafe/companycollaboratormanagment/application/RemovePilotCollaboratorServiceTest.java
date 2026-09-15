package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.domain.SkillsAssessment;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import eapli.framework.infrastructure.authz.domain.model.Username;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

// assertThrows retained for NullPointerException tests (requireNonNull guards)

@ExtendWith(MockitoExtension.class)
class RemovePilotCollaboratorServiceTest {

    private static final AirTransportCompany TAP = new AirTransportCompany(
            CompanyName.valueOf("TAP"), IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
    private static final AirTransportCompany OTHER = new AirTransportCompany(
            CompanyName.valueOf("Other"), IATACode.valueOf("FR"), ICAOCode.valueOf("FRA"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);

    private final TestAircraftRepositoryFactory repositoryFactory = new TestAircraftRepositoryFactory();
    private IsolatedPilotUserRepository pilots;
    private FlightRepository flights;
    private RemovePilotCollaboratorService service;

    @Mock
    private TransactionalContext txCtx;

    @BeforeEach
    void setUp() {
        pilots = new IsolatedPilotUserRepository();
        flights = repositoryFactory.flights();
        service = new RemovePilotCollaboratorService(pilots, flights);
    }

    @Test
    void constructorRejectsNullRepositories() {
        assertThrows(IllegalArgumentException.class, () -> new RemovePilotCollaboratorService(null, flights));
        assertThrows(IllegalArgumentException.class, () -> new RemovePilotCollaboratorService(pilots, null));
    }

    @Test
    void deactivatePilotSucceedsWhenNoActiveFlightPlans() {
        final PilotUser pilot = savePilot("ok@tap.com", TAP);

        service.deactivatePilot(EmailAddress.valueOf("ok@tap.com"), TAP, NOW, txCtx);

        assertFalse(pilot.systemUser().isActive());
        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
    }

    @Test
    void deactivatePilotReturnsHasActiveFlightsWhenDraftFutureFlight() {
        final PilotUser pilot = savePilot("busy@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPBUSY1", FlightPlanStatus.DRAFT, NOW.plusDays(1)));

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("busy@tap.com"), TAP, NOW, txCtx);
        assertEquals(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS, result.outcome());
        assertTrue(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotReturnsHasActiveFlightsWhenSimApprovedFutureFlight() {
        final PilotUser pilot = savePilot("approved@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPAPPR1", FlightPlanStatus.SIM_APPROVED, NOW.plusDays(2)));

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("approved@tap.com"), TAP, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS, result.outcome());
        assertTrue(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotSucceedsWhenDraftPlanButFlightInPast() {
        final PilotUser pilot = savePilot("past@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPPAST1", FlightPlanStatus.DRAFT, NOW.minusDays(1)));

        service.deactivatePilot(EmailAddress.valueOf("past@tap.com"), TAP, NOW, null);

        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotSucceedsWhenSimApprovedButFlightInPast() {
        final PilotUser pilot = savePilot("pastappr@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPPA1", FlightPlanStatus.SIM_APPROVED, NOW.minusDays(3)));

        service.deactivatePilot(EmailAddress.valueOf("pastappr@tap.com"), TAP, NOW, null);

        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotSucceedsWhenFlightPlansAreInTerminalState() {
        final PilotUser pilot = savePilot("done@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPDONE1", FlightPlanStatus.SIM_REJECTED, NOW.plusDays(1)));

        service.deactivatePilot(EmailAddress.valueOf("done@tap.com"), TAP, NOW, null);

        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotReturnsNotFoundWhenPilotMissing() {
        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("missing@tap.com"), TAP, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.NOT_FOUND, result.outcome());
    }

    @Test
    void deactivatePilotReturnsNotInRosterWhenWrongCompany() {
        savePilot("foreign@tap.com", TAP);

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("foreign@tap.com"), OTHER, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.NOT_IN_ROSTER, result.outcome());
    }

    @Test
    void deactivatePilotReturnsAlreadyInactiveWhenPilotInactive() {
        final PilotUser pilot = savePilot("off@tap.com", TAP);
        pilot.systemUser().deactivate(Calendar.getInstance());
        pilots.save(pilot);

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("off@tap.com"), TAP, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.ALREADY_INACTIVE, result.outcome());
    }

    @Test
    void deactivatePilotRequiresEmail() {
        assertThrows(NullPointerException.class, () -> service.deactivatePilot(null, TAP, NOW, null));
    }

    @Test
    void deactivatePilotRequiresCompany() {
        savePilot("a@tap.com", TAP);
        assertThrows(NullPointerException.class,
                () -> service.deactivatePilot(EmailAddress.valueOf("a@tap.com"), null, NOW, null));
    }

    @Test
    void deactivatePilotRequiresReferenceTime() {
        savePilot("a@tap.com", TAP);
        assertThrows(NullPointerException.class,
                () -> service.deactivatePilot(EmailAddress.valueOf("a@tap.com"), TAP, null, null));
    }

    @Test
    void deactivatePilotWithoutTransactionalContextSkipsTransactionCalls() {
        savePilot("notx@tap.com", TAP);

        service.deactivatePilot(EmailAddress.valueOf("notx@tap.com"), TAP, NOW, null);

        verifyNoInteractions(txCtx);
    }

    @Test
    void deactivatePilotDoesNotCommitWhenConstraintViolated() {
        final PilotUser pilot = savePilot("rollback@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPRB1", FlightPlanStatus.DRAFT, NOW.plusHours(1)));

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("rollback@tap.com"), TAP, NOW, txCtx);

        assertEquals(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS, result.outcome());
        verify(txCtx).beginTransaction();
        verify(txCtx).rollback();
        verify(txCtx).close();
        verify(txCtx, never()).commit();
        assertTrue(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotThreeArgOverloadSucceeds() {
        final PilotUser pilot = savePilot("overload@tap.com", TAP);

        final DeactivatePilotResult result = service.deactivatePilot(
                EmailAddress.valueOf("overload@tap.com"), TAP, null);

        assertEquals(DeactivatePilotResult.Outcome.SUCCESS, result.outcome());
        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotReturnsHasActiveFlightsWhenSubmittedForSimulation() {
        final PilotUser pilot = savePilot("submitted@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPSUB1", FlightPlanStatus.SUBMITTED_FOR_SIMULATION, NOW.plusDays(1)));

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("submitted@tap.com"), TAP, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS, result.outcome());
    }

    @Test
    void deactivatePilotReturnsHasActiveFlightsWhenDepartureExactlyAtReferenceTime() {
        final PilotUser pilot = savePilot("edge@tap.com", TAP);
        flights.save(flightWithPlan(
                pilot, TAP, "TPEDGE1", FlightPlanStatus.DRAFT, NOW));

        final DeactivatePilotResult result = service.deactivatePilot(EmailAddress.valueOf("edge@tap.com"), TAP, NOW, null);
        assertEquals(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS, result.outcome());
    }

    @Test
    void deactivatePilotSucceedsWhenDraftPlanButNoSchedule() {
        final PilotUser pilot = savePilot("nosched@tap.com", TAP);
        final FlightDesignator designator = new FlightDesignator("TPNS1");
        final Flight flight = new Flight(designator, "R1", "CS-NS");
        flight.assignPilotId(pilot);
        flight.assignFlightPlan(FlightPlan.forFlight(
                designator, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), "{}"));
        flights.save(flight);

        service.deactivatePilot(EmailAddress.valueOf("nosched@tap.com"), TAP, NOW, null);

        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivatePilotSucceedsWhenBlockingFlightBelongsToAnotherPilot() {
        final PilotUser target = savePilot("target@tap.com", TAP);
        final PilotUser other = savePilot("other@tap.com", TAP);
        flights.save(flightWithPlan(
                other, TAP, "TPOTH1", FlightPlanStatus.DRAFT, NOW.plusDays(1)));

        service.deactivatePilot(EmailAddress.valueOf("target@tap.com"), TAP, NOW, null);

        assertFalse(target.systemUser().isActive());
        assertTrue(other.systemUser().isActive());
    }

    @Test
    void deactivatePilotPersistsThroughRepository() {
        final PilotUser pilot = savePilot("persist@tap.com", TAP);

        final DeactivatePilotResult result = service.deactivatePilot(
                EmailAddress.valueOf("persist@tap.com"), TAP, NOW, null);

        assertEquals(DeactivatePilotResult.Outcome.SUCCESS, result.outcome());
        assertFalse(pilot.systemUser().isActive());
        assertFalse(pilots.findByEmail(EmailAddress.valueOf("persist@tap.com"))
                .orElseThrow()
                .systemUser()
                .isActive());
    }

    private PilotUser savePilot(final String email, final AirTransportCompany company) {
        final PilotUser pilot = new PilotUser(
                buildUser(email),
                SecurityClearance.valueOf(LocalDate.of(2027, 1, 1)),
                company,
                Phone.valueOf("+351999999999"),
                SkillsAssessment.valueOf(LocalDate.of(2024, 1, 1)));
        return pilots.save(pilot);
    }

    private static CompanyCollaboratorUser collaboratorForSameUser(
            final PilotUser pilot, final AirTransportCompany company) {
        return new CompanyCollaboratorUser(
                pilot.systemUser(),
                pilot.securityClearance(),
                company,
                pilot.phoneNumber(),
                pilot.skillsAssessment());
    }

    private static Flight flightWithPlan(
            final PilotUser pilot,
            final AirTransportCompany company,
            final String designatorCode,
            final FlightPlanStatus planStatus,
            final LocalDateTime departure) {
        final FlightDesignator designator = new FlightDesignator(designatorCode);
        final Flight flight = new Flight(designator, "R1", "CS-TST");
        flight.assignPilotId(pilot);
        flight.assignSchedule(new FlightSchedule(departure, departure.plusHours(2)));
        flight.assignFlightPlan(FlightPlan.forFlight(
                designator, planStatus, new FuelLoad(1000.0), "{}"));
        return flight;
    }

    private static SystemUser buildUser(final String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "John", "Doe", email)
                .build();
    }

    static final class IsolatedPilotUserRepository implements PilotUserRepository {
        private final Map<AISafeUserId, PilotUser> byId = new HashMap<>();

        @Override
        public PilotUser save(final PilotUser entity) {
            byId.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<PilotUser> ofIdentity(final AISafeUserId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Iterable<PilotUser> findAll() {
            return byId.values();
        }

        @Override
        public boolean containsOfIdentity(final AISafeUserId id) {
            return byId.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final AISafeUserId id) {
            byId.remove(id);
        }

        @Override
        public void delete(final PilotUser entity) {
            deleteOfIdentity(entity.identity());
        }

        @Override
        public long count() {
            return byId.size();
        }

        @Override
        public Iterable<PilotUser> findPilotByCompanyAndActive(final AirTransportCompany company) {
            return byId.values().stream()
                    .filter(p -> p.airTransportCompany().identity().equals(company.identity()))
                    .filter(p -> p.systemUser().isActive())
                    .toList();
        }

        @Override
        public Optional<PilotUser> findByUsername(final Username username) {
            return byId.values().stream()
                    .filter(p -> p.systemUser().username().equals(username))
                    .findFirst();
        }

        @Override
        public Optional<PilotUser> findByEmail(final EmailAddress email) {
            return byId.values().stream()
                    .filter(p -> p.systemUser().email().equals(email))
                    .findFirst();
        }

        @Override
        public Optional<PilotUser> findByEmailWithLock(final EmailAddress email,
                                                       final AirTransportCompany company) {
            return byId.values().stream()
                    .filter(p -> p.systemUser().email().equals(email))
                    .filter(p -> p.airTransportCompany().identity().equals(company.identity()))
                    .findFirst();
        }
    }
}
