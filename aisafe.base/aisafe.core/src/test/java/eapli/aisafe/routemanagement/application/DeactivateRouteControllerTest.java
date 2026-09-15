package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateRouteControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock
    private DeactivateRouteService service;
    @Mock
    private CompanyCollaboratorUserRepository collaborators;
    @Mock
    private UserSession session;
    @Mock
    private SystemUser systemUser;
    @Mock
    private CompanyCollaboratorUser collaborator;

    private final AirTransportCompany company = RouteTestFixtures.COMPANY_TP;

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    private DeactivateRouteController controller() {
        final DeactivateRouteController ctrl = new DeactivateRouteController(authz, service, collaborators);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("user1"));
        when(collaborators.findByUsername(Username.valueOf("user1"))).thenReturn(Optional.of(collaborator));
        when(collaborator.airTransportCompany()).thenReturn(company);
        return ctrl;
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();
        assertDoesNotThrow(() -> new DeactivateRouteController());
    }

    @Test
    void listActiveRouteOptionsDelegatesToService() {
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                company,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        final ActiveRouteOption option = new ActiveRouteOption(route, java.util.Optional.of(LocalDate.of(2026, 8, 1)));
        when(service.listActiveRouteOptionsByCompany(company)).thenReturn(List.of(option));

        assertEquals(1, controller().listActiveRouteOptions().size());
        verify(service).listActiveRouteOptionsByCompany(company);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void deactivateRouteDelegatesToService() {
        final LocalDate date = LocalDate.of(2026, 12, 1);
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                company,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        when(service.deactivateRoute("TP123", date, company)).thenReturn(route);

        assertEquals(route, controller().deactivateRoute("TP123", date));
        verify(service).deactivateRoute("TP123", date, company);
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new DeactivateRouteController(null, service, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new DeactivateRouteController(authz, null, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new DeactivateRouteController(authz, service, null));
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = eapli.aisafe.Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties props = (Properties) propertiesField.get(eapli.aisafe.Application.settings());
            props.setProperty("persistence.repositoryFactory",
                    "eapli.aisafe.routemanagement.TestRouteControllersRepositoryFactory");

            final Field factoryField = eapli.aisafe.infrastructure.persistence.PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void ensureAuthzRegistryConfigured() {
        try {
            eapli.framework.infrastructure.authz.application.AuthzRegistry.authorizationService();
        } catch (IllegalStateException ignored) {
            eapli.framework.infrastructure.authz.application.AuthzRegistry.configure(
                    mock(eapli.framework.infrastructure.authz.domain.repositories.UserRepository.class),
                    mock(eapli.framework.infrastructure.authz.domain.model.PasswordPolicy.class),
                    mock(org.springframework.security.crypto.password.PasswordEncoder.class));
        }
    }

    private void resetAuthzRegistry() {
        try {
            final Field authorizationSvc = eapli.framework.infrastructure.authz.application.AuthzRegistry.class
                    .getDeclaredField("authorizationSvc");
            authorizationSvc.setAccessible(true);
            authorizationSvc.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }

    private void resetPersistenceFactory() {
        try {
            final Field factoryField = eapli.aisafe.infrastructure.persistence.PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }
}
