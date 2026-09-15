package eapli.aisafe.flightcontroloperatormanagement.application;

import eapli.aisafe.Application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddFlightControlOperatorControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock private FlightControlOperatorUserService flightControlOperatorUserService;

    @InjectMocks
    private AddFlightControlOperatorController controller;

    private Set<Role> rolesWithFCO() {
        return new HashSet<>(Set.of(AISafeRoles.FLIGHT_CONTROL_OPERATOR));
    }

    @Test
    void addUserEnsuresAuthorizationAndDelegatesToService() {
        controller.addUser("jdoe", "Pass1234", "John", "Doe",
                "jdoe@fco.com", rolesWithFCO(), "AREA-01", "2025-01-01", "2025-01-01", "+351999999999");

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verify(flightControlOperatorUserService).createCollaboratorUser(
                eq("jdoe"), eq("Pass1234"), eq("John"), eq("Doe"),
                eq("jdoe@fco.com"), eq(rolesWithFCO()), any(Calendar.class),
                eq("AREA-01"), eq("2025-01-01"), eq("2025-01-01"), eq("+351999999999"),
                any(), any(), any(), any());
    }

    @Test
    void addUserFailsWhenUserIsUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class,
                () -> controller.addUser("jdoe", "Pass1234", "John", "Doe",
                        "jdoe@fco.com", rolesWithFCO(), "AREA-01", "2025-01-01", "2025-01-01", "+351999999999"));

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verifyNoInteractions(flightControlOperatorUserService);
    }

    @Test
    void constructorNullGuards() {
        assertThrows(IllegalArgumentException.class,
                () -> new AddFlightControlOperatorController(null, flightControlOperatorUserService));
        assertThrows(IllegalArgumentException.class,
                () -> new AddFlightControlOperatorController(authz, null));
    }

    @Test
    void addUserPropagatesServiceExceptionAfterAuthorization() {
        doThrow(new IllegalArgumentException("Area not found"))
                .when(flightControlOperatorUserService).createCollaboratorUser(
                        any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class,
                () -> controller.addUser("jdoe", "Pass1234", "John", "Doe",
                        "jdoe@fco.com", rolesWithFCO(), "AREA-99", "2025-01-01", "2025-01-01", "+351999999999"));

        verify(flightControlOperatorUserService).createCollaboratorUser(
                any(), any(), any(), any(), any(), any(), any(),
                eq("AREA-99"), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void addUserDoesNotCallServiceWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class,
                () -> controller.addUser("jdoe2", "Pass1234", "Jane", "Doe",
                        "jdoe2@fco.com", rolesWithFCO(), "AREA-02", "2025-06-01", "2025-06-01", "+351999999999"));

        verifyNoInteractions(flightControlOperatorUserService);
    }

    @Test
    void getEligibleUsersEnsuresAuthorizationAndDelegatesToService() {
        SystemUser userMock = mock(SystemUser.class);
        when(userMock.email()).thenReturn(EmailAddress.valueOf("test@fco.com"));
        when(flightControlOperatorUserService.findEligibleUsersForFCO(any(), any()))
                .thenReturn(List.of(userMock));

        List<String> result = controller.getEligibleUsers();

        verify(authz).isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        assertEquals(1, result.size());
        assertEquals("test@fco.com", result.get(0));
    }

    @Test
    void getEligibleUsersFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class, controller::getEligibleUsers);

        verifyNoInteractions(flightControlOperatorUserService);
    }

    @Test
    void addFCOToExistingUserEnsuresAuthorizationAndDelegatesToService() {
        controller.addFCOToExistingUser("existing@email.com", "AREA-01", "2025-01-01", "2025-01-01", "+351912345678");

        verify(authz).isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        verify(flightControlOperatorUserService).createFCOOnly(
                eq("existing@email.com"), eq("AREA-01"), eq("2025-01-01"), eq("2025-01-01"), eq("+351912345678"),
                any(), any(), any(), any());
    }

    @Test
    void addFCOToExistingUserFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class,
                () -> controller.addFCOToExistingUser("existing@email.com", "AREA-01", "2025-01-01", "2025-01-01", "+351912345678"));

        verifyNoInteractions(flightControlOperatorUserService);
    }
}
