package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.Application;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateRouteControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock
    private RouteService service;
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

    private CreateRouteController controller() {
        final CreateRouteController ctrl = new CreateRouteController(authz, service, collaborators);
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

        assertDoesNotThrow(() -> new CreateRouteController());
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new CreateRouteController(null, service, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new CreateRouteController(authz, null, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new CreateRouteController(authz, service, null));
    }

    @Test
    void currentCompanyContextReturnsCompanyIata() {
        assertEquals("TP", controller().currentCompanyContext());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void validateRouteNameDelegatesWhenCompanyMatches() {
        when(service.validateRouteName("TP123")).thenReturn("TP123");

        assertEquals("TP123", controller().validateRouteName("123", "TP"));
        verify(service).validateRouteName("TP123");
    }

    @Test
    void validateRouteNameFailsWhenCompanyMismatch() {
        assertThrows(IllegalArgumentException.class, () -> controller().validateRouteName("123", "FR"));
    }

    @Test
    void listAirportsDelegatesToService() {
        final Iterable<Airport> airports = List.of(RouteTestFixtures.AIRPORT_OPO);
        when(service.listAirports()).thenReturn(airports);

        assertEquals(airports, controller().listAirports());
    }

    @Test
    void createCharterRouteDelegatesToService() {
        final Route route = Route.charterRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                company,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.charterSchedule());
        final LocalDate departure = LocalDate.of(2026, 6, 1);
        final LocalDate arrival = LocalDate.of(2026, 6, 2);
        when(service.createCharterRoute("TP123", "OPO", "LIS", company, departure, arrival)).thenReturn(route);

        assertEquals(route, controller().createCharterRoute("TP123", "OPO", "LIS", departure, arrival));
        verify(service).createCharterRoute("TP123", "OPO", "LIS", company, departure, arrival);
    }

    @Test
    void createRegularRouteDelegatesToService() {
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                company,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        final List<DayOfWeek> days = List.of(DayOfWeek.MONDAY);
        when(service.createRegularRoute(eq("TP123"), eq("OPO"), eq("LIS"), eq(company), eq(days))).thenReturn(route);

        assertEquals(route, controller().createRegularRoute("TP123", "OPO", "LIS", days));
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties props = (Properties) propertiesField.get(Application.settings());
            props.setProperty("persistence.repositoryFactory",
                    "eapli.aisafe.routemanagement.TestRouteControllersRepositoryFactory");

            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void ensureAuthzRegistryConfigured() {
        try {
            AuthzRegistry.authorizationService();
        } catch (IllegalStateException ignored) {
            AuthzRegistry.configure(mock(UserRepository.class), mock(PasswordPolicy.class), mock(PasswordEncoder.class));
        }
    }

    private void resetAuthzRegistry() {
        try {
            final Field authorizationSvc = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            authorizationSvc.setAccessible(true);
            authorizationSvc.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }

    private void resetPersistenceFactory() {
        try {
            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }
}
