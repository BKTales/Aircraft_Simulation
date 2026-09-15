package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.companycollaboratormanagment.application.PilotCollaboratorUserService;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidateFlightPlanControllerTest {

    private AuthorizationService authz;
    private ValidateFlightPlanService service;
    private PilotUserRepository pilots;
    private PilotCollaboratorUserService pilotService;
    private ValidateFlightPlanController controller;
    private SystemUser pilotUser;

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        service = mock(ValidateFlightPlanService.class);
        pilots = mock(PilotUserRepository.class);
        pilotService = mock(PilotCollaboratorUserService.class);
        controller = new ValidateFlightPlanController(authz, service, pilots, pilotService);
        pilotUser = mock(SystemUser.class);
        final UserSession session = mock(UserSession.class);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(pilotUser);
        when(pilotUser.username()).thenReturn(Username.valueOf("pilot1"));
    }

    @Test
    void ensureValidateRequiresPilotRole() {
        when(service.validate(eq("TP123"), eq(pilotUser), isNull()))
                .thenReturn(ValidateFlightPlanResult.approved("TP123", List.of()));
        when(pilotService.findPilot(pilots, Username.valueOf("pilot1"))).thenReturn(mock(PilotUser.class));

        final ValidateFlightPlanResult result = controller.validateFlightPlan("TP123");

        assertEquals(FlightPlanStatus.SIM_APPROVED, result.status());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).validate("TP123", pilotUser, null);
    }

    @Test
    void ensureListValidatableDelegatesToServiceForAuthenticatedPilot() {
        final FlightValidationPreview preview = new FlightValidationPreview(
                "TP123", "TP123", "OPO", "LIS", "CS-TP01",
                FlightPlanStatus.DRAFT, null, null);
        when(pilotService.findPilot(pilots, Username.valueOf("pilot1"))).thenReturn(mock(PilotUser.class));
        when(service.listValidatableForPilot(pilotUser)).thenReturn(List.of(preview));

        final List<FlightValidationPreview> result = controller.listValidatableFlights();

        assertEquals(1, result.size());
        assertEquals("TP123", result.get(0).designator());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(service).listValidatableForPilot(pilotUser);
    }

    @Test
    void ensureValidateForwardsOnSimulationStartCallback() {
        final Runnable callback = mock(Runnable.class);
        when(pilotService.findPilot(pilots, Username.valueOf("pilot1"))).thenReturn(mock(PilotUser.class));
        when(service.validate(eq("TP123"), eq(pilotUser), eq(callback)))
                .thenReturn(ValidateFlightPlanResult.approved("TP123", List.of()));

        controller.validateFlightPlan("TP123", callback);

        verify(service).validate("TP123", pilotUser, callback);
    }

    @Test
    void ensureValidateFailsWhenSessionMissing() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.validateFlightPlan("TP123"));
    }

    @Test
    void ensureUnauthorizedUserCannotList() {
        org.mockito.Mockito.doThrow(new IllegalStateException("denied"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);

        assertThrows(IllegalStateException.class, () -> controller.listValidatableFlights());
        verify(service, org.mockito.Mockito.never()).listValidatableForPilot(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new ValidateFlightPlanController(null, service, pilots, pilotService));
    }
}
