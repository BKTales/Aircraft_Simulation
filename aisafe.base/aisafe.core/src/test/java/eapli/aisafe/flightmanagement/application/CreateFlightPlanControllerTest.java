package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFlightPlanControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private CreateFlightPlanService service;

    private CreateFlightPlanController controller;
    private Username sessionUsername;

    @BeforeEach
    void setUp() {
        controller = new CreateFlightPlanController(authz, service);
        sessionUsername = Username.valueOf("pilot1");
    }

    @Test
    void constructorThrowsWhenAuthzIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreateFlightPlanController(null, service));
    }

    @Test
    void constructorThrowsWhenServiceIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreateFlightPlanController(authz, null));
    }

    @Test
    void createFlightPlan_delegatesToServiceWhenAuthorized() {
        final CreateFlightPlanRequest request = sampleRequest();
        final CreateFlightPlanResult expected = CreateFlightPlanResult.success(new FlightDesignator("TP123"));
        when(service.createFlightPlan(request)).thenReturn(expected);

        final CreateFlightPlanResult result = controller.createFlightPlan(request);

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).createFlightPlan(request);
    }

    @Test
    void createFlightPlan_throwsWhenUnauthorized() {
        doThrow(new IllegalStateException("not authorized"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);

        assertThrows(IllegalStateException.class,
                () -> controller.createFlightPlan(sampleRequest()));

        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verifyNoInteractions(service);
    }

    @Test
    void listSelectableRoutes_passesSessionUsernameAndDate() {
        mockPilotSession(sessionUsername);
        final LocalDate asOf = LocalDate.of(2026, 6, 1);
        final List<Route> expected = List.of(mock(Route.class));
        when(service.listSelectableRoutes(sessionUsername, asOf)).thenReturn(expected);

        final List<Route> result = controller.listSelectableRoutes(asOf);

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).listSelectableRoutes(sessionUsername, asOf);
    }

    @Test
    void listCompanyActiveAircraftRegistrations_delegatesWithSessionUsername() {
        mockPilotSession(sessionUsername);
        final List<String> expected = List.of("CS-TP01");
        when(service.listCompanyActiveAircraftRegistrations(sessionUsername)).thenReturn(expected);

        final List<String> result = controller.listCompanyActiveAircraftRegistrations();

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).listCompanyActiveAircraftRegistrations(sessionUsername);
    }

    @Test
    void listCompanyPilots_delegatesWithSessionUsername() {
        mockPilotSession(sessionUsername);
        final List<PilotUser> expected = List.of(mock(PilotUser.class));
        when(service.listCompanyPilots(sessionUsername)).thenReturn(expected);

        final List<PilotUser> result = controller.listCompanyPilots();

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).listCompanyPilots(sessionUsername);
    }

    @Test
    void throwsWhenNoAuthenticatedSession() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> controller.listSelectableRoutes(LocalDate.of(2026, 6, 1)));
        assertThrows(IllegalStateException.class,
                () -> controller.listCompanyActiveAircraftRegistrations());
        assertThrows(IllegalStateException.class,
                () -> controller.listCompanyPilots());

        verifyNoInteractions(service);
    }

    private void mockPilotSession(final Username username) {
        final UserSession session = mock(UserSession.class);
        final SystemUser systemUser = mock(SystemUser.class);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.username()).thenReturn(username);
    }

    private static CreateFlightPlanRequest sampleRequest() {
        return new CreateFlightPlanRequest(
                "TP123",
                "CS-TP01",
                "pilot1",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                FuelQuantity.kilograms(5000),
                0,
                0,
                0,
                Optional.empty());
    }
}
