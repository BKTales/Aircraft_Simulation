package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import eapli.aisafe.usermanagement.domain.AISafeRoles;

class RegisterAirTransportCompanyControllerTest {

    @Test
    void registerCompanyEnsuresAuthorizationAndDelegatesToService() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final AirTransportCompanyService service = mock(AirTransportCompanyService.class);
        final RegisterAirTransportCompanyController controller = new RegisterAirTransportCompanyController(authz, service);

        controller.registerCompany("TAP Air Portugal", "TP", "TAP");

        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        verify(service).registerCompany("TAP Air Portugal", "TP", "TAP");
    }

    @Test
    void registerCompanyFailsWhenUserIsUnauthorized() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        final AirTransportCompanyService service = mock(AirTransportCompanyService.class);
        final RegisterAirTransportCompanyController controller = new RegisterAirTransportCompanyController(authz, service);

        assertThrows(IllegalStateException.class,
                () -> controller.registerCompany("TAP Air Portugal", "TP", "TAP"));
    }

    @Test
    void constructorRejectsNullAuthorizationService() {
        final AirTransportCompanyService service = mock(AirTransportCompanyService.class);
        assertThrows(IllegalArgumentException.class,
                () -> new RegisterAirTransportCompanyController(null, service));
    }

    @Test
    void constructorRejectsNullService() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        assertThrows(IllegalArgumentException.class,
                () -> new RegisterAirTransportCompanyController(authz, null));
    }
}
