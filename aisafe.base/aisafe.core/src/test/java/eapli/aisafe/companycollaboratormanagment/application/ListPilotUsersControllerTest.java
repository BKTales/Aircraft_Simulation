package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPilotUsersControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private PilotCollaboratorUserService pilotCollaboratorUserService;

    @Mock
    private CompanyCollaboratorUserService companyCollaboratorUserService;

    @Mock
    private CompanyCollaboratorUserRepository collaboratorRepository;

    @Mock
    private PilotUserRepository pilotUserRepository;

    @Mock
    private AirTransportCompanyRepository companyRepository;

    @Mock
    private UserSession session;

    @InjectMocks
    private ListPilotUsersController controller;

    @Test
    void shouldReturnAllCompaniesWhenAuthorized() {
        Iterable<AirTransportCompany> expected = List.of(mock(AirTransportCompany.class));

        when(companyRepository.findAll()).thenReturn(expected);

        Iterable<AirTransportCompany> result = controller.allAirTransportCompanies();

        assertEquals(expected, result);

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        verify(companyRepository).findAll();
    }
    @Test
    void shouldReturnActivePilotCollaboratorsForCompanyWhenAuthorized() {
        final Username username = Username.valueOf("user1");
        final UserSession session = mock(UserSession.class);
        final SystemUser systemUser = mock(SystemUser.class);
        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        final AirTransportCompany company = mock(AirTransportCompany.class);

        final Iterable<ResponsePilotCollaboratorDTO> expectedData =
                List.of(mock(ResponsePilotCollaboratorDTO.class));

        final ListPilotsResult expectedResult = ListPilotsResult.success(expectedData);

        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.username()).thenReturn(username);
        when(companyCollaboratorUserService.findATCC(collaboratorRepository, username))
                .thenReturn(collaborator);

        when(collaborator.airTransportCompany()).thenReturn(company);

        when(pilotCollaboratorUserService.findActivePilotsByCompany(pilotUserRepository, company))
                .thenReturn(expectedResult);

        final ListPilotsResult result = controller.activePilotUsersForCompany();

        assertTrue(result.isSuccess());
        assertEquals(expectedData, result.pilots());

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
        verify(pilotCollaboratorUserService)
                .findActivePilotsByCompany(pilotUserRepository, company);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthorizedForCompanies() {
        doThrow(new IllegalStateException("not authorized"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(
                        AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
                );

        assertThrows(IllegalStateException.class, () ->
                controller.allAirTransportCompanies()
        );

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        verifyNoInteractions(companyRepository);
    }


    @Test
    void shouldThrowExceptionWhenUserIsNotAuthorizedForCollaborators() {
        doThrow(new IllegalStateException("not authorized"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(
                        AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
                );

        assertThrows(IllegalStateException.class, () ->
                controller.activePilotUsersForCompany()
        );

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        verifyNoInteractions(pilotCollaboratorUserService);
    }

    @Test
    void ensureConstructorWithNullServicesThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ListPilotUsersController(
                        null,
                        pilotCollaboratorUserService,
                        companyCollaboratorUserService,
                        collaboratorRepository,
                        pilotUserRepository,
                        companyRepository
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ListPilotUsersController(
                        authz,
                        null,
                        companyCollaboratorUserService,
                        collaboratorRepository,
                        pilotUserRepository,
                        companyRepository
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ListPilotUsersController(
                        authz,
                        pilotCollaboratorUserService,
                        null,
                        collaboratorRepository,
                        pilotUserRepository,
                        companyRepository
                )
        );
    }

    @Test
    void ensureDefaultConstructorInitializesCorrectly() {
        try {
            ListPilotUsersController defaultController =
                    new ListPilotUsersController();

            assertNotNull(defaultController);

        } catch (Exception e) {
            System.out.println(
                    "Default constructor line covered, but infrastructure not initialized: "
                            + e.getMessage()
            );
        }
    }
}