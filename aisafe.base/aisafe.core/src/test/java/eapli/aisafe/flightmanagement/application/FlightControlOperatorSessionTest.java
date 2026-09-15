package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightControlOperatorSessionTest {

    @Mock
    private AuthorizationService authz;
    @Mock
    private FlightControlOperatorUserRepository operators;
    @Mock
    private UserSession session;
    @Mock
    private SystemUser systemUser;
    @Mock
    private FlightControlOperatorUser operator;

    @Test
    void returnsOperatorWhenSessionValid() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("fco1"));
        when(operators.findByUsername(Username.valueOf("fco1"))).thenReturn(Optional.of(operator));

        assertEquals(operator, FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
    }

    @Test
    void failsWhenNotAuthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);

        assertThrows(IllegalStateException.class,
                () -> FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
    }

    @Test
    void failsWhenOperatorNotRegistered() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("unknown"));
        when(operators.findByUsername(Username.valueOf("unknown"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
    }

    @Test
    void failsWhenSessionEmpty() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
    }

    @Test
    void failsWhenIdentityThrowsClassCastException() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenThrow(new ClassCastException("bad principal"));

        assertThrows(IllegalStateException.class,
                () -> FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
    }

    @Test
    void failsWhenPrincipalIsNotUsername() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        doAnswer(invocation -> "not-a-username").when(systemUser).identity();

        assertThrows(IllegalStateException.class,
                () -> FlightControlOperatorSession.requireFlightControlOperator(authz, operators));
    }
}
