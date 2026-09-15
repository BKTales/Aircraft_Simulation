package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.aircraftmanagement.application.DecommissionAircraftController;
import eapli.aisafe.aircraftmanagement.application.ListFleetController;
import eapli.aisafe.aircraftmanagement.application.RegisterAircraftController;
import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.application.AddPilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.DeactivatePilotResult;
import eapli.aisafe.companycollaboratormanagment.application.ListPilotUsersController;
import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;
import eapli.aisafe.companycollaboratormanagment.application.RemovePilotCollaboratorController;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.routemanagement.application.CreateRouteController;
import eapli.aisafe.routemanagement.application.DeactivateRouteController;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AtccCommandHandlerTest {

    @Mock private AuthorizationService authz;
    @Mock private UserSession session;
    @Mock private ClientHandler clientSession;
    @Mock private RegisterAircraftController registerAircraft;
    @Mock private DecommissionAircraftController decommissionAircraft;
    @Mock private ListFleetController listFleet;
    @Mock private ListAircraftModelsController listAircraftModels;
    @Mock private AddPilotCollaboratorController addPilot;
    @Mock private ListPilotUsersController listPilots;
    @Mock private RemovePilotCollaboratorController removePilot;
    @Mock private CreateRouteController createRoute;
    @Mock private DeactivateRouteController deactivateRoute;

    private AtccCommandHandler handler;

    @BeforeEach
    void setUp() {
        configureAuthzRegistry(authz);
        when(authz.session()).thenReturn(Optional.of(session));
        when(authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)).thenReturn(true);
        when(session.authenticatedUser()).thenReturn(mock(SystemUser.class));

        handler = new AtccCommandHandler(clientSession,
                registerAircraft, decommissionAircraft, listFleet, listAircraftModels,
                addPilot, listPilots, removePilot, createRoute, deactivateRoute);
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
    }

    @Test
    void ensureRequiresSession() throws Exception {
        when(authz.session()).thenReturn(Optional.empty());
        assertEquals(ResponseCodes.UNAUTHORIZED, invoke(AtccOpcodes.LIST_ELIGIBLE_USERS, "").opcode());
    }

    @Test
    void ensureRequiresAtccRole() throws Exception {
        when(authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)).thenReturn(false);
        assertEquals(ResponseCodes.FORBIDDEN, invoke(AtccOpcodes.LIST_ELIGIBLE_USERS, "").opcode());
    }

    @Test
    void ensureAddPilotExistingRejectsShortPayload() throws Exception {
        final ProtocolFrame resp = invoke(AtccOpcodes.ADD_PILOT_EXISTING_USER, "p@tap.pt;2026-01-01");
        assertEquals(ResponseCodes.BAD_REQUEST, resp.opcode());
    }

    @Test
    void ensureListActiveRoutesReturnsRoutes() throws Exception {
        final Route r = route("TP100");
        when(deactivateRoute.listActiveRoutes()).thenReturn(List.of(r));
        final ProtocolFrame resp = invoke(AtccOpcodes.LIST_ACTIVE_ROUTES, "");
        assertEquals(ResponseCodes.OK, resp.opcode());
        assertTrue(resp.payload().startsWith("TP100|TP|OPO|LIS|CHARTER"));
    }

    @Test
    void ensureDeactivateRouteDelegates() throws Exception {
        final LocalDate date = LocalDate.of(2026, 6, 15);
        final Route r = route("TP100");
        when(deactivateRoute.deactivateRoute("TP100", date)).thenReturn(r);
        final ProtocolFrame resp = invoke(AtccOpcodes.DEACTIVATE_ROUTE, "TP100;2026-06-15");
        assertEquals(ResponseCodes.OK, resp.opcode());
        assertTrue(resp.payload().startsWith("OK|TP100"));
        verify(deactivateRoute).deactivateRoute("TP100", date);
    }

    @Test
    void ensureDeactivateRouteRejectsMissingDate() throws Exception {
        final ProtocolFrame resp = invoke(AtccOpcodes.DEACTIVATE_ROUTE, "TP100");
        assertEquals(ResponseCodes.BAD_REQUEST, resp.opcode());
    }

    @Test
    void ensureRemovePilotSuccessReturnsOk() throws Exception {
        final eapli.aisafe.companycollaboratormanagment.domain.PilotUser mockPilot =
                mock(eapli.aisafe.companycollaboratormanagment.domain.PilotUser.class);
        when(mockPilot.toDTO()).thenReturn(mock(eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO.class));
        final DeactivatePilotResult success = DeactivatePilotResult.success(mockPilot);
        when(removePilot.deactivatePilot("p@tap.pt")).thenReturn(success);
        final ProtocolFrame resp = invoke(AtccOpcodes.REMOVE_PILOT, "p@tap.pt");
        assertEquals(ResponseCodes.OK, resp.opcode());
        assertEquals("Pilot deactivated.", resp.payload());
    }

    @Test
    void ensureRemovePilotNotFoundReturnsNotFound() throws Exception {
        when(removePilot.deactivatePilot("missing@tap.pt"))
                .thenReturn(DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.NOT_FOUND));
        final ProtocolFrame resp = invoke(AtccOpcodes.REMOVE_PILOT, "missing@tap.pt");
        assertEquals(ResponseCodes.NOT_FOUND, resp.opcode());
    }

    @Test
    void ensureRemovePilotHasActiveFlightsReturnsConflict() throws Exception {
        when(removePilot.deactivatePilot("busy@tap.pt"))
                .thenReturn(DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS));
        final ProtocolFrame resp = invoke(AtccOpcodes.REMOVE_PILOT, "busy@tap.pt");
        assertEquals(ResponseCodes.CONFLICT, resp.opcode());
    }

    @Test
    void ensureRemovePilotEmptyPayloadReturnsBadRequest() throws Exception {
        final ProtocolFrame resp = invoke(AtccOpcodes.REMOVE_PILOT, "");
        assertEquals(ResponseCodes.BAD_REQUEST, resp.opcode());
    }

    @Test
    void ensureUnknownOpcodeReturnsBadRequest() throws Exception {
        assertEquals(ResponseCodes.BAD_REQUEST, invoke((byte) 99, "").opcode());
    }

    @Test
    void ensureUnexpectedRuntimeExceptionReturnsInternalError() throws Exception {
        when(removePilot.deactivatePilot(any(String.class)))
                .thenThrow(new RuntimeException("DB connection lost"));
        final ProtocolFrame resp = invoke(AtccOpcodes.REMOVE_PILOT, "pilot@tap.pt");
        assertEquals(ResponseCodes.INTERNAL_ERROR, resp.opcode());
    }

    private static Route route(final String name) {
        final Route route = mock(Route.class);
        when(route.identity()).thenReturn(RouteName.valueOf(name));
        when(route.companyIATACode()).thenReturn(IATACode.valueOf("TP"));
        when(route.originAirportIATACode()).thenReturn(AirportIATACode.valueOf("OPO"));
        when(route.destinationAirportIATACode()).thenReturn(AirportIATACode.valueOf("LIS"));
        when(route.flightType()).thenReturn(FlightType.CHARTER);
        return route;
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
}
