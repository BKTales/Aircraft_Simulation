package eapli.aisafe.routemanagement.application;

import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyRouteCollaboratorSessionTest {

    @Mock
    private AuthorizationService authz;
    @Mock
    private CompanyCollaboratorUserRepository collaborators;
    @Mock
    private UserSession session;
    @Mock
    private SystemUser systemUser;
    @Mock
    private CompanyCollaboratorUser collaborator;

    @Test
    void returnsCollaboratorWhenSessionValid() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("user1"));
        when(collaborators.findByUsername(Username.valueOf("user1"))).thenReturn(Optional.of(collaborator));

        assertEquals(collaborator, CompanyRouteCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void failsWhenNotAuthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        assertThrows(IllegalStateException.class,
                () -> CompanyRouteCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators));
    }

    @Test
    void failsWhenSessionEmpty() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> CompanyRouteCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators));
    }

    @Test
    void failsWhenCollaboratorNotRegistered() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("unknown"));
        when(collaborators.findByUsername(Username.valueOf("unknown"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> CompanyRouteCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators));
    }

    @Test
    void failsWhenIdentityThrowsClassCastException() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenThrow(new ClassCastException("bad principal"));

        assertThrows(IllegalStateException.class,
                () -> CompanyRouteCollaboratorSession.requireAirTransportCompanyCollaborator(authz, collaborators));
    }
}
