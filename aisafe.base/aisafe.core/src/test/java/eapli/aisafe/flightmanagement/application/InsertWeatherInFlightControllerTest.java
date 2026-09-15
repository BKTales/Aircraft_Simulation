package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.Application;
import eapli.aisafe.flightmanagement.Us082TestRepositoryFactory;
import eapli.aisafe.flightmanagement.domain.*;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InsertWeatherInFlightControllerTest {

    @Test
    void attachWeatherToFlightUpdatesPlanAndPersists() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final FlightDesignator designator = new FlightDesignator("TP1234A");
        final Flight flight = new Flight(designator, "OPO-LIS", "CS-TST");
        flight.assignFlightPlan(FlightPlan.forFlight(designator, FlightPlanStatus.SIM_APPROVED, FuelLoad.valueOf(1000), "{\"ok\":true}"));
        final WeatherData weatherData = mockWeather(7L);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(7L)).thenReturn(Optional.of(weatherData));
        when(flightRepository.save(flight)).thenReturn(flight);

        final Flight result = controller.attachWeatherToFlight("TP1234A", 7L);

        assertEquals(7L, result.weatherData().identity());
        assertEquals(FlightPlanStatus.DRAFT, result.flightPlan().status());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
    }

    @Test
    void constructorRejectsNullDependencies() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);

        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherInFlightController(null, flightRepository, weatherDataRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherInFlightController(authz, null, weatherDataRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherInFlightController(authz, flightRepository, null));
    }

    @Test
    void attachWeatherFailsWhenFlightIsMissing() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        when(flightRepository.findByDesignator(any(FlightDesignator.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.attachWeatherToFlight("TP404", 1L));
    }

    @Test
    void attachWeatherFailsWhenWeatherDataIsMissing() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final FlightDesignator designator = new FlightDesignator("TP1234A");
        final Flight flight = new Flight(designator, "OPO-LIS", "CS-TST");
        flight.assignFlightPlan(FlightPlan.forFlight(designator, FlightPlanStatus.DRAFT, FuelLoad.valueOf(1000), "{\"ok\":true}"));
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.attachWeatherToFlight("TP1234A", 999L));
    }

    @Test
    void weatherDataByIdDelegatesAndReturnsOptional() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final WeatherData weatherData = mockWeather(3L);
        when(weatherDataRepository.ofIdentity(3L)).thenReturn(Optional.of(weatherData));

        final Optional<WeatherData> result = controller.weatherDataById(3L);

        assertTrue(result.isPresent());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
        verify(weatherDataRepository).ofIdentity(3L);
    }

    @Test
    void attachWeatherKeepsDraftStatusWhenPlanAlreadyDraft() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final FlightDesignator designator = new FlightDesignator("TP2000A");
        final Flight flight = new Flight(designator, "OPO-LIS", "CS-TST");
        flight.assignFlightPlan(FlightPlan.forFlight(designator, FlightPlanStatus.DRAFT, FuelLoad.valueOf(1000), "{\"ok\":true}"));
        final WeatherData weatherData = mockWeather(9L);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(9L)).thenReturn(Optional.of(weatherData));
        when(flightRepository.save(flight)).thenReturn(flight);

        final Flight result = controller.attachWeatherToFlight("TP2000A", 9L);

        assertEquals(FlightPlanStatus.DRAFT, result.flightPlan().status());
        assertEquals(9L, result.weatherData().identity());
    }

    @Test
    void attachWeatherFailsWhenFlightHasNoPlan() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final FlightDesignator designator = new FlightDesignator("TP3000A");
        final Flight flight = new Flight(designator, "OPO-LIS", "CS-TST");
        final WeatherData weatherData = mockWeather(1L);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(1L)).thenReturn(Optional.of(weatherData));

        assertThrows(IllegalStateException.class, () -> controller.attachWeatherToFlight("TP3000A", 1L));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void attachWeatherRequiresPilotRole() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        doThrow(new SecurityException("not allowed"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);

        assertThrows(SecurityException.class, () -> controller.attachWeatherToFlight("TP1234A", 1L));
        verifyNoInteractions(flightRepository, weatherDataRepository);
    }

    @Test
    void weatherDataByIdReturnsEmptyWhenMissing() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        when(weatherDataRepository.ofIdentity(404L)).thenReturn(Optional.empty());

        final Optional<WeatherData> result = controller.weatherDataById(404L);

        assertTrue(result.isEmpty());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.PILOT);
    }

    @Test
    void defaultConstructorInitializesDependencies() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> {
            final InsertWeatherInFlightController controller = new InsertWeatherInFlightController();
            assertNotNull(controller);
        });
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    @Test
    void attachWeatherResetsPlanToDAFTWhenSimRejected() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final FlightRepository flightRepository = mock(FlightRepository.class);
        final WeatherDataRepository weatherDataRepository = mock(WeatherDataRepository.class);
        final InsertWeatherInFlightController controller = new InsertWeatherInFlightController(authz, flightRepository, weatherDataRepository);
        final FlightDesignator designator = new FlightDesignator("TP9999A");
        final Flight flight = new Flight(designator, "OPO-LIS", "CS-TST");
        flight.assignFlightPlan(FlightPlan.forFlight(designator, FlightPlanStatus.SIM_REJECTED, FuelLoad.valueOf(1000), "{\"ok\":true}"));
        final WeatherData weatherData = mockWeather(5L);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(5L)).thenReturn(Optional.of(weatherData));
        when(flightRepository.save(flight)).thenReturn(flight);

        final Flight result = controller.attachWeatherToFlight("TP9999A", 5L);

        assertEquals(FlightPlanStatus.DRAFT, result.flightPlan().status());
    }

    private static WeatherData mockWeather(final Long id) {
        final WeatherData weatherData = mock(WeatherData.class);
        when(weatherData.identity()).thenReturn(id);
        return weatherData;
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties properties = (Properties) propertiesField.get(Application.settings());
            properties.setProperty("persistence.repositoryFactory", Us082TestRepositoryFactory.class.getName());

            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException e) {
            fail("Unable to configure repository factory for test", e);
        }
    }

    private void ensureAuthzRegistryConfigured() {
        try {
            AuthzRegistry.authorizationService();
        } catch (IllegalStateException ignored) {
            final UserRepository userRepository = mock(UserRepository.class);
            final PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
            final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
            AuthzRegistry.configure(userRepository, passwordPolicy, passwordEncoder);
        }
    }

    private void resetAuthzRegistry() {
        try {
            final Field authorizationSvc = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            authorizationSvc.setAccessible(true);
            authorizationSvc.set(null, null);

            final Field authenticationService = AuthzRegistry.class.getDeclaredField("authenticationService");
            authenticationService.setAccessible(true);
            authenticationService.set(null, null);

            final Field userService = AuthzRegistry.class.getDeclaredField("userService");
            userService.setAccessible(true);
            userService.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
    }

    private void resetPersistenceFactory() {
        try {
            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
    }
}
