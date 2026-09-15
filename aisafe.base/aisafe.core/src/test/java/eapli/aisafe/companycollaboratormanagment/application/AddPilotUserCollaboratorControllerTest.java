package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.CreatePilotCollaboratorDTO;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AddPilotUserCollaboratorControllerTest {

    @Mock private AuthorizationService authz;
    @Mock private PilotCollaboratorUserService pilotService;
    @Mock private CompanyCollaboratorUserService collaboratorUserService;

    @InjectMocks
    private AddPilotCollaboratorController controller;

    private void mockSession() {
        UserSession session = mock(UserSession.class);
        SystemUser systemUser = mock(SystemUser.class);
        CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        AirTransportCompany company = mock(AirTransportCompany.class);

        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.username()).thenReturn(Username.valueOf("user1"));
        when(collaboratorUserService.findATCC(any(), any())).thenReturn(collaborator);
        when(collaborator.airTransportCompany()).thenReturn(company);
    }

    private CreatePilotCollaboratorDTO baseDto(String securityDate, String phoneNumber, List<PilotCertificationSpec> certs) {
        CreatePilotCollaboratorDTO dto = mock(CreatePilotCollaboratorDTO.class);
        when(dto.getUsername()).thenReturn("pilot");
        when(dto.getPassword()).thenReturn("Pass1234");
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Doe");
        when(dto.getEmail()).thenReturn("pilot@company.com");
        when(dto.getSecurityClearanceStartDate()).thenReturn(securityDate);
        when(dto.getSkillDate()).thenReturn("2024-01-01");
        when(dto.getPhoneNumber()).thenReturn(phoneNumber);
        when(dto.getCertifications()).thenReturn(certs);
        return dto;
    }

    private void stubService(AddPilotResult result) {
        when(pilotService.createPilotUser(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), anyString(), anyString(), anyString(),
                any(), any(), any(), any(), any()))
                .thenReturn(result);
    }

    @Test
    void addUserEnsuresAuthorizationAndDelegatesToService() {
        mockSession();
        stubService(AddPilotResult.failure(AddPilotResult.Outcome.SESSION_NOT_FOUND));

        List<PilotCertificationSpec> certs = List.of(new PilotCertificationSpec("A320", "2025-01-01", "2026-01-01"));
        CreatePilotCollaboratorDTO dto = baseDto("2027-01-01", "+351999999999", certs);

        AddPilotResult result = controller.addUser(dto);

        assertNotNull(result);
        verify(pilotService).createPilotUser(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), anyString(), anyString(), anyString(),
                any(), any(), any(), any(), any());
    }

    @Test
    void addUserFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        CreatePilotCollaboratorDTO dto = mock(CreatePilotCollaboratorDTO.class);

        assertThrows(IllegalStateException.class, () -> controller.addUser(dto));
        verifyNoInteractions(pilotService);
    }

    @Test
    void addUserThrowsWhenATCCNotFound() {
        UserSession session = mock(UserSession.class);
        SystemUser user = mock(SystemUser.class);

        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(user);
        when(user.username()).thenReturn(Username.valueOf("user1"));
        when(collaboratorUserService.findATCC(any(), any()))
                .thenThrow(new IllegalStateException("No collaborator linked"));

        CreatePilotCollaboratorDTO dto = mock(CreatePilotCollaboratorDTO.class);

        assertThrows(IllegalStateException.class, () -> controller.addUser(dto));
    }

    @Test
    void getAircraftModelIdsDelegatesToService() {
        when(pilotService.getAircraftModelIds(any())).thenReturn(new String[]{"A320", "B737"});
        String[] result = controller.getAircraftModelIds();
        assertArrayEquals(new String[]{"A320", "B737"}, result);
    }

    @Test
    void addUserThrowsWhenNoSession() {
        when(authz.session()).thenReturn(Optional.empty());
        CreatePilotCollaboratorDTO dto = mock(CreatePilotCollaboratorDTO.class);

        assertThrows(NoSuchElementException.class, () -> controller.addUser(dto));
    }

    @Test
    void addUserReturnsSuccessWhenPilotCreatedSuccessfully() {
        mockSession();
        stubService(AddPilotResult.success());

        List<PilotCertificationSpec> certs = List.of(new PilotCertificationSpec("A320", "2025-01-01", "2026-01-01"));
        AddPilotResult result = controller.addUser(baseDto("2027-01-01", "+351912345678", certs));

        assertEquals(AddPilotResult.Outcome.SUCCESS, result.outcome());
    }

    @Test
    void addUserReturnsDuplicateUsernameWhenUserAlreadyExists() {
        mockSession();
        stubService(AddPilotResult.failure(AddPilotResult.Outcome.DUPLICATE_USERNAME));

        CreatePilotCollaboratorDTO dto = mock(CreatePilotCollaboratorDTO.class);
        when(dto.getUsername()).thenReturn("existingpilot");
        when(dto.getPassword()).thenReturn("Pass1234");
        when(dto.getFirstName()).thenReturn("Jane");
        when(dto.getLastName()).thenReturn("Doe");
        when(dto.getEmail()).thenReturn("jane@company.com");
        when(dto.getSecurityClearanceStartDate()).thenReturn("2027-01-01");
        when(dto.getSkillDate()).thenReturn("2024-01-01");
        when(dto.getPhoneNumber()).thenReturn("+351912345678");
        when(dto.getCertifications()).thenReturn(List.of(new PilotCertificationSpec("A320", "2025-01-01", "2026-01-01")));

        AddPilotResult result = controller.addUser(dto);

        assertEquals(AddPilotResult.Outcome.DUPLICATE_USERNAME, result.outcome());
    }

    @Test
    void addUserReturnsAircraftModelNotFoundWhenModelMissing() {
        mockSession();
        stubService(AddPilotResult.failure(AddPilotResult.Outcome.AIRCRAFT_MODEL_NOT_FOUND));

        List<PilotCertificationSpec> certs = List.of(new PilotCertificationSpec("INVALID", "2025-01-01", "2026-01-01"));
        AddPilotResult result = controller.addUser(baseDto("2027-01-01", "+351912345678", certs));

        assertEquals(AddPilotResult.Outcome.AIRCRAFT_MODEL_NOT_FOUND, result.outcome());
    }

    @Test
    void addUserReturnsNoCertificationsWhenNoCertsProvided() {
        mockSession();
        stubService(AddPilotResult.failure(AddPilotResult.Outcome.NO_CERTIFICATIONS));

        AddPilotResult result = controller.addUser(baseDto("2027-01-01", "+351912345678", List.of()));

        assertEquals(AddPilotResult.Outcome.NO_CERTIFICATIONS, result.outcome());
    }

    @Test
    void addUserReturnsInvalidDateFormatWhenDateInvalid() {
        mockSession();
        stubService(AddPilotResult.failure(AddPilotResult.Outcome.INVALID_DATE_FORMAT));

        List<PilotCertificationSpec> certs = List.of(new PilotCertificationSpec("A320", "invalid", "invalid"));
        AddPilotResult result = controller.addUser(baseDto("invalid-date", "+351912345678", certs));

        assertEquals(AddPilotResult.Outcome.INVALID_DATE_FORMAT, result.outcome());
    }
}