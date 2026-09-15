package eapli.aisafe.rcomp.server.pilot;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanController;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanRequest;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanResult;
import eapli.aisafe.flightmanagement.application.ImportFlightPlanFromFileController;
import eapli.aisafe.flightmanagement.application.InsertWeatherInFlightController;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanController;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanResult;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.rcomp.protocol.PilotCreateFlightPlanPayload;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.ValidateFlightPlanDslFailurePayload;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PilotCommandHandlerTest {

    private TestAuthorizationService authz;
    @Mock
    private UserSession session;
    @Mock
    private ClientHandler clientSession;
    @Mock
    private CreateFlightPlanController createFlightPlanController;
    @Mock
    private InsertWeatherInFlightController insertWeatherController;
    @Mock
    private ImportFlightPlanFromFileController importFlightPlanController;
    @Mock
    private ValidateFlightPlanController validateFlightPlanController;
    @Mock
    private FlightRepository flightRepository;

    private PilotCommandHandler handler;

    @BeforeEach
    void setUp() {
        authz = new TestAuthorizationService();
        authz.sessionOpt = Optional.of(session);
        authz.pilotAuthorized = true;
        configureAuthzRegistry(authz);
        when(session.authenticatedUser()).thenReturn(mock(SystemUser.class));

        handler = new PilotCommandHandler(
                clientSession,
                createFlightPlanController,
                insertWeatherController,
                importFlightPlanController,
                validateFlightPlanController,
                flightRepository);
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
    }

    @Test
    void ensureRequiresSession() throws Exception {
        authz.sessionOpt = Optional.empty();
        final ProtocolFrame response = invoke(PilotOpcodes.CREATE_FLIGHT_PLAN, "");
        assertEquals(ResponseCodes.UNAUTHORIZED, response.opcode());
    }

    @Test
    void ensureRequiresPilotRole() throws Exception {
        authz.pilotAuthorized = false;
        final ProtocolFrame response = invoke(PilotOpcodes.CREATE_FLIGHT_PLAN, "");
        assertEquals(ResponseCodes.FORBIDDEN, response.opcode());
    }

    @Test
    void ensureCreatePlanSucceeds() throws Exception {
        when(createFlightPlanController.createFlightPlan(any(CreateFlightPlanRequest.class)))
                .thenReturn(CreateFlightPlanResult.success(new FlightDesignator("TP123")));

        final String payload = PilotCreateFlightPlanPayload.encode(new PilotCreateFlightPlanPayload.Fields(
                "TP123", "CS-TP01", "pilot1",
                "2026-06-01 10:00", "2026-06-01 12:00",
                5000.0, "kg", 120, 10000.0, 500.0, "", false));

        final ProtocolFrame response = invoke(PilotOpcodes.CREATE_FLIGHT_PLAN, payload);

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().startsWith("OK|TP123|DRAFT|"));
    }

    @Test
    void ensureCreatePlanNeedsConfirmation() throws Exception {
        when(createFlightPlanController.createFlightPlan(any(CreateFlightPlanRequest.class)))
                .thenReturn(CreateFlightPlanResult.needsConfirmation(
                        new FlightDesignator("TP123"),
                        eapli.aisafe.flightmanagement.domain.FlightPlanStatus.SIM_APPROVED));

        final String payload = PilotCreateFlightPlanPayload.encode(new PilotCreateFlightPlanPayload.Fields(
                "TP123", "CS-TP01", "pilot1",
                "2026-06-01 10:00", "2026-06-01 12:00",
                5000.0, "kg", 120, 10000.0, 500.0, "", false));

        final ProtocolFrame response = invoke(PilotOpcodes.CREATE_FLIGHT_PLAN, payload);

        assertEquals(ResponseCodes.NEEDS_CONFIRMATION, response.opcode());
        assertTrue(response.payload().contains("SIM_APPROVED"));
    }

    @Test
    void ensureCreatePlanFailsWhenControllerRejects() throws Exception {
        when(createFlightPlanController.createFlightPlan(any(CreateFlightPlanRequest.class)))
                .thenReturn(CreateFlightPlanResult.failure("Arrival must be after departure."));

        final String payload = PilotCreateFlightPlanPayload.encode(new PilotCreateFlightPlanPayload.Fields(
                "TP123", "CS-TP01", "pilot1",
                "2026-06-01 12:00", "2026-06-01 10:00",
                5000.0, "kg", 0, 0.0, 0.0, "", false));

        final ProtocolFrame response = invoke(PilotOpcodes.CREATE_FLIGHT_PLAN, payload);

        assertEquals(ResponseCodes.BAD_REQUEST, response.opcode());
        assertTrue(response.payload().toLowerCase().contains("arrival"));
    }

    @Test
    void ensureListCreateRoutesReturnsActiveRoutes() throws Exception {
        final Route route = mock(Route.class);
        when(route.identity()).thenReturn(eapli.aisafe.routemanagement.domain.RouteName.valueOf("TP123"));
        when(route.originAirport()).thenReturn(mock(eapli.aisafe.airportmanagement.domain.Airport.class));
        when(route.destinationAirport()).thenReturn(mock(eapli.aisafe.airportmanagement.domain.Airport.class));
        when(route.originAirport().identity())
                .thenReturn(eapli.aisafe.airportmanagement.domain.AirportIATACode.valueOf("OPO"));
        when(route.destinationAirport().identity())
                .thenReturn(eapli.aisafe.airportmanagement.domain.AirportIATACode.valueOf("LIS"));
        when(route.flightType()).thenReturn(eapli.aisafe.flightmanagement.domain.FlightType.CHARTER);
        when(createFlightPlanController.listSelectableRoutes(LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(route));

        final ProtocolFrame response = invoke(PilotOpcodes.LIST_CREATE_ROUTES, "2026-06-01");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().contains("TP123|OPO|LIS|CHARTER"));
    }

    @Test
    void ensureValidateFlightPlanPasses() throws Exception {
        when(validateFlightPlanController.validateFlightPlan("TP123"))
                .thenReturn(ValidateFlightPlanResult.approved("TP123", List.of()));

        final ProtocolFrame response = invoke(PilotOpcodes.VALIDATE_FLIGHT_PLAN, "TP123");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertEquals("OK|PASS|TP123", response.payload());
    }

    @Test
    void ensureValidateFlightPlanFailsWhenSimulationRejects() throws Exception {
        when(validateFlightPlanController.validateFlightPlan("TP123"))
                .thenReturn(ValidateFlightPlanResult.rejected("TP123", "Simulation failed.", List.of()));

        final ProtocolFrame response = invoke(PilotOpcodes.VALIDATE_FLIGHT_PLAN, "TP123");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertEquals("OK|FAIL|TP123|Simulation failed.", response.payload());
    }

    @Test
    void ensureValidateFlightPlanReturnsBadRequestOnDslFailure() throws Exception {
        when(validateFlightPlanController.validateFlightPlan("TP123"))
                .thenReturn(ValidateFlightPlanResult.dslFailure("TP123", List.of("line 1: syntax error"), "flight TP123 {"));

        final ProtocolFrame response = invoke(PilotOpcodes.VALIDATE_FLIGHT_PLAN, "TP123");

        assertEquals(ResponseCodes.BAD_REQUEST, response.opcode());
        assertTrue(ValidateFlightPlanDslFailurePayload.matches(response.payload()));
        final ValidateFlightPlanDslFailurePayload.Decoded decoded =
                ValidateFlightPlanDslFailurePayload.decode(response.payload());
        assertEquals("flight TP123 {", decoded.dslContent());
        assertEquals(List.of("line 1: syntax error"), decoded.errors());
    }

    @Test
    void ensureValidateFlightPlanReturnsFailWhenBlocked() throws Exception {
        when(validateFlightPlanController.validateFlightPlan("TP123"))
                .thenReturn(ValidateFlightPlanResult.blocked("TP123", "Only the owner pilot may validate this flight plan."));

        final ProtocolFrame response = invoke(PilotOpcodes.VALIDATE_FLIGHT_PLAN, "TP123");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertEquals("OK|FAIL|TP123|Only the owner pilot may validate this flight plan.", response.payload());
    }

    @Test
    void ensureListCompanyPilotsReturnsRoster() throws Exception {
        final PilotUser pilot = mock(PilotUser.class);
        final SystemUser user = mock(SystemUser.class);
        when(pilot.systemUser()).thenReturn(user);
        when(user.username()).thenReturn(Username.valueOf("pilot1"));
        final eapli.framework.infrastructure.authz.domain.model.Name name =
                mock(eapli.framework.infrastructure.authz.domain.model.Name.class);
        when(user.name()).thenReturn(name);
        when(name.firstName()).thenReturn("João");
        when(name.lastName()).thenReturn("Silva");
        when(user.email()).thenReturn(eapli.framework.general.domain.model.EmailAddress.valueOf("pilot1@tap.pt"));
        when(createFlightPlanController.listCompanyPilots()).thenReturn(List.of(pilot));

        final ProtocolFrame response = invoke(PilotOpcodes.LIST_COMPANY_PILOTS, "");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().contains("pilot1|João|Silva|pilot1@tap.pt"));
    }

    private ProtocolFrame invoke(final byte opcode, final String payload) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        handler.handle(opcode, payload, new DataOutputStream(baos));
        return ProtocolFrame.read(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
    }

    private static void configureAuthzRegistry(final AuthorizationService authz) {
        try {
            final Field field = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            field.setAccessible(true);
            field.set(null, authz);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void resetAuthzRegistry() {
        try {
            final Field field = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            field.setAccessible(true);
            field.set(null, null);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class TestAuthorizationService extends AuthorizationService {
        private Optional<UserSession> sessionOpt = Optional.empty();
        private boolean pilotAuthorized = true;

        @Override
        public Optional<UserSession> session() {
            return sessionOpt;
        }

        @Override
        public boolean isAuthenticatedUserAuthorizedTo(final Role... roles) {
            if (roles == null) {
                return false;
            }
            for (final Role role : roles) {
                if (AISafeRoles.PILOT.equals(role)) {
                    return pilotAuthorized;
                }
            }
            return false;
        }
    }
}
