package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.Application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCompanyCollaboratorControllerTest {

    @Mock private AuthorizationService authz;
    @Mock private CompanyCollaboratorUserService collaboratorUserService;
    @InjectMocks private AddCompanyCollaboratorController controller;



    private Set<Role> rolesWithFCO() {
        return new HashSet<>(Set.of(AISafeRoles.FLIGHT_CONTROL_OPERATOR));
    }


    @Test
    void addUserEnsuresAuthorizationAndDelegatesToService() {
        Set<Role> roles = rolesWithFCO();

        controller.addUser("jdoe", "Pass1234", "John", "Doe",
                "jdoe@fco.com", roles, "AREA-01", "2025-01-01", "2025-01-01", "+351999999999");

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verify(collaboratorUserService).createCollaboratorUser(
                eq("jdoe"), eq("Pass1234"), eq("John"), eq("Doe"),
                eq("jdoe@fco.com"), eq(roles), any(Calendar.class),
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
        verifyNoInteractions(collaboratorUserService);
    }

    @Test
    void addUserPropagatesServiceExceptionAfterAuthorization() {
        doThrow(new IllegalArgumentException("Area not found"))
                .when(collaboratorUserService).createCollaboratorUser(
                        any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class,
                () -> controller.addUser("jdoe", "Pass1234", "John", "Doe",
                        "jdoe@fco.com", rolesWithFCO(), "AREA-99", "2025-01-01", "2025-01-01", "+351999999999"));

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verify(collaboratorUserService).createCollaboratorUser(
                any(), any(), any(), any(), any(), any(), any(),
                eq("AREA-99"), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void addUserDoesNotCallServiceWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class,
                () -> controller.addUser("jdoe2", "Pass1234", "Jane", "Doe",
                        "jdoe2@fco.com", rolesWithFCO(), "AREA-02", "2025-06-01",  "2025-06-01","+351999999999"));

        verifyNoInteractions(collaboratorUserService);
    }



    @Test
    void ensureGetEligibleUsersChecksAuthAndMapsEmails() {
        SystemUser userMock = mock(SystemUser.class);
        when(userMock.email()).thenReturn(EmailAddress.valueOf("eligible@company.com"));
        when(collaboratorUserService.findEligibleUsersForATCC(any(), any()))
                .thenReturn(List.of(userMock));

        List<String> result = controller.getEligibleUsers();

        verify(authz).isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        assertEquals(1, result.size());
        assertEquals("eligible@company.com", result.get(0));
    }

    @Test
    void getEligibleUsersFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class, controller::getEligibleUsers);

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verifyNoInteractions(collaboratorUserService);
    }

    @Test
    void addATCCToExistingUserEnsuresAuthorizationAndDelegatesToService() {
        controller.addATCCToExistingUser("existing@company.com", "TP", "2025-01-01", "2025-01-01", "+351999999999");

        verify(authz).isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR);
        verify(collaboratorUserService).createATCCOnly(
                eq("existing@company.com"), eq("TP"), eq("2025-01-01"), eq("2025-01-01"), eq("+351999999999"),
                any(), any(), any(),any());
    }

    @Test
    void addATCCToExistingUserFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).isAuthenticatedUserAuthorizedTo(any());

        assertThrows(IllegalStateException.class,
                () -> controller.addATCCToExistingUser("existing@company.com", "TP", "2025-01-01", "2025-01-01", "+351999999999"));

        verify(authz).isAuthenticatedUserAuthorizedTo(any());
        verifyNoInteractions(collaboratorUserService);
    }

}
