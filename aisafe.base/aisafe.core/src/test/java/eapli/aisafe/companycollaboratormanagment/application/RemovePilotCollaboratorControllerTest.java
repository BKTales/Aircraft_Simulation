package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemovePilotCollaboratorControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private RemovePilotCollaboratorService removePilotService;

    @Mock
    private PilotCollaboratorUserService pilotListingService;

    @Mock
    private CompanyCollaboratorUserService collaboratorUserService;

    @Mock
    private CompanyCollaboratorUserRepository collaborators;

    @Mock
    private eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository pilotRepository;

    @Mock
    private TransactionalContext txCtx;

    @InjectMocks
    private RemovePilotCollaboratorController controller;

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(null, removePilotService, pilotListingService,
                        collaboratorUserService, collaborators, pilotRepository, txCtx));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(authz, null, pilotListingService,
                        collaboratorUserService, collaborators, pilotRepository, txCtx));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(authz, removePilotService, null,
                        collaboratorUserService, collaborators, pilotRepository, txCtx));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(authz, removePilotService, pilotListingService,
                        null, collaborators, pilotRepository, txCtx));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(authz, removePilotService, pilotListingService,
                        collaboratorUserService, null, pilotRepository, txCtx));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotCollaboratorController(authz, removePilotService, pilotListingService,
                        collaboratorUserService, collaborators, null, txCtx));
    }

    @Test
    void deactivatePilotEnsuresAuthorizationAndDelegatesToService() {
        stubAtccSession("atcc1");
        final DeactivatePilotResult expected = DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.NOT_FOUND);
        when(removePilotService.deactivatePilot(
                eq(EmailAddress.valueOf("pilot@tap.com")),
                any(AirTransportCompany.class),
                eq(txCtx))).thenReturn(expected);

        assertSame(expected, controller.deactivatePilot("pilot@tap.com"));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void deactivatePilotFailsWhenUserIsUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        assertThrows(IllegalStateException.class, () -> controller.deactivatePilot("pilot@tap.com"));
    }

    @Test
    void listActivePilotsChecksAuthAndReturnsDTOs() {
        stubAtccSession("atcc1");
        final List<ResponsePilotCollaboratorDTO> expectedData = List.of(mock(ResponsePilotCollaboratorDTO.class));
        final ListPilotsResult resultEnvelope = ListPilotsResult.success(expectedData);

        when(pilotListingService.findActivePilotsByCompany(eq(pilotRepository), any(AirTransportCompany.class)))
                .thenReturn(resultEnvelope);


        final Iterable<ResponsePilotCollaboratorDTO> result = controller.listActivePilotsForCompany();
        assertEquals(expectedData, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    private void stubAtccSession(final String username) {
        final UserSession session = mock(UserSession.class);
        final SystemUser systemUser = mock(SystemUser.class);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.username()).thenReturn(Username.valueOf(username));

        final CompanyCollaboratorUser atcc = mock(CompanyCollaboratorUser.class);
        when(atcc.airTransportCompany()).thenReturn(tapCompany());
        when(collaboratorUserService.findATCC(collaborators, Username.valueOf(username))).thenReturn(atcc);
    }

    private static AirTransportCompany tapCompany() {
        return new AirTransportCompany(
                CompanyName.valueOf("TAP"), IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
    }
}
