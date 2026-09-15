package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.domain.*;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.*;
import eapli.aisafe.Application;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.application.RegisterWeatherDataController;
import eapli.aisafe.weatherdata.application.RegisterWeatherResult;
import eapli.aisafe.weatherdata.application.WeatherDataService;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterWeatherDataControllerTest {

    private AuthorizationService authz;
    private WeatherDataService weatherService;
    private AirControlAreaService areaService;
    private RegisterWeatherDataController controller;
    private AirControlArea area;

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        weatherService = mock(WeatherDataService.class);
        areaService = mock(AirControlAreaService.class);

        controller = new RegisterWeatherDataController(authz, weatherService, areaService);

        area = new AirControlArea(
                new AirControlAreaName("Test Area"),
                new GeographicBoundary(List.of(
                        new GeographicCoords(38.0f, -9.0f),
                        new GeographicCoords(39.0f, -9.0f),
                        new GeographicCoords(38.0f, -8.0f)
                )),
                new MinFuelRequirement(100)
        );
    }

    @Test
    void testAvailableAreas() {
        when(areaService.availableAreas()).thenReturn(List.of(area));

        var areas = controller.availableAreas();

        assertNotNull(areas);
        assertTrue(areas.iterator().hasNext());
        verify(areaService).availableAreas();
    }

    @Test
    void testRegisterWeatherDataSuccess() {
        List<float[]> coords = List.of(new float[]{10, 10}, new float[]{20, 10}, new float[]{10, 20});
        WeatherData expected = mock(WeatherData.class);
        RegisterWeatherResult serviceResult = RegisterWeatherResult.success(expected);

        when(weatherService.registerWeatherData(
                eq(area.identity().toString()),
                anyList(),
                eq(20f),
                eq(180),
                eq(10f),
                eq(50f),
                eq(1013f),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).thenReturn(serviceResult);

        var result = controller.registerWeatherData(
                area.identity().toString(), coords, 20f, 180, 10f, 50f, 1013f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        );

        assertSame(serviceResult, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        verify(weatherService).registerWeatherData(
                eq(area.identity().toString()),
                anyList(),
                eq(20f),
                eq(180),
                eq(10f),
                eq(50f),
                eq(1013f),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        );
    }

    @Test
    void testRegisterWeatherDataFailsWhenUnauthorized() {
        doThrow(new SecurityException("not allowed"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);

        List<float[]> coords = List.of(new float[]{10, 10}, new float[]{20, 10}, new float[]{10, 20});

        assertThrows(SecurityException.class, () -> controller.registerWeatherData(
                area.identity().toString(), coords, 20f, 180, 10f, 50f, 1013f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        ));

        verify(weatherService, never()).registerWeatherData(
                anyString(), anyList(), anyFloat(), anyInt(), anyFloat(), anyFloat(), anyFloat(), any(), any(), any()
        );
    }

    @Test
    void testConstructorNullGuards() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterWeatherDataController(null, weatherService, areaService));
        assertThrows(IllegalArgumentException.class, () -> new RegisterWeatherDataController(authz, null, areaService));
        assertThrows(IllegalArgumentException.class, () -> new RegisterWeatherDataController(authz, weatherService, null));
    }

    @Test
    void testDefaultConstructor() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> {
            RegisterWeatherDataController defaultController = new RegisterWeatherDataController();
            assertNotNull(defaultController.availableAreas());
        });
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            Properties properties = (Properties) propertiesField.get(Application.settings());
            properties.setProperty("persistence.repositoryFactory", TestWeatherDataRepositoryFactory.class.getName());

            Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
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
            UserRepository userRepository = mock(UserRepository.class);
            PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AuthzRegistry.configure(userRepository, passwordPolicy, passwordEncoder);
        }
    }

    private void resetAuthzRegistry() {
        try {
            Field authorizationSvc = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            authorizationSvc.setAccessible(true);
            authorizationSvc.set(null, null);

            Field authenticationService = AuthzRegistry.class.getDeclaredField("authenticationService");
            authenticationService.setAccessible(true);
            authenticationService.set(null, null);

            Field userService = AuthzRegistry.class.getDeclaredField("userService");
            userService.setAccessible(true);
            userService.set(null, null);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void resetPersistenceFactory() {
        try {
            Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
