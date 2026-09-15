package eapli.aisafe.weatherdata;

import eapli.aisafe.Application;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.application.BulkImportWeatherResult;
import eapli.aisafe.weatherdata.application.BulkWeatherDataController;
import eapli.aisafe.weatherdata.application.ConsultWeatherResult;
import eapli.aisafe.weatherdata.application.WeatherDataService;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReaderFactory;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class BulkWeatherDataControllerTest {

    @Test
    void importFromFileImportsEveryRecord() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final BulkWeatherDataController controller = new BulkWeatherDataController(authz, weatherDataService, factory);
        final WeatherData saved = mock(WeatherData.class);
        when(weatherDataService.importFromFile(any(), eq(factory), isNull()))
                .thenReturn(BulkImportWeatherResult.success(List.of(saved, saved)));

        final BulkImportWeatherResult result = controller.importFromFile(Path.of("dummy.csv"));

        assertTrue(result.isSuccess());
        assertEquals(2, result.imported().size());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        verify(weatherDataService).importFromFile(eq(Path.of("dummy.csv")), eq(factory), isNull());
    }

    @Test
    void consultWeatherDataForDayRequiresAuthorizationAndDelegates() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final BulkWeatherDataController controller = new BulkWeatherDataController(authz, weatherDataService, factory);
        final LocalDateTime day = LocalDateTime.of(2026, 6, 22, 10, 0);
        when(weatherDataService.consultWeatherDataForDay(eq("121"), eq(day)))
                .thenReturn(ConsultWeatherResult.success(List.of()));

        final ConsultWeatherResult result = controller.consultWeatherDataForDay("121", day);

        assertTrue(result.isSuccess());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        verify(weatherDataService).consultWeatherDataForDay("121", day);
    }

    @Test
    void nullDependenciesAreRejected() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);

        assertThrows(IllegalArgumentException.class, () -> new BulkWeatherDataController(null, weatherDataService, factory));
        assertThrows(IllegalArgumentException.class, () -> new BulkWeatherDataController(authz, null, factory));
        assertThrows(IllegalArgumentException.class, () -> new BulkWeatherDataController(authz, weatherDataService, null));
    }

    @Test
    void importFromFileReturnsIoErrorOutcome() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final BulkWeatherDataController controller = new BulkWeatherDataController(authz, weatherDataService, factory);
        when(weatherDataService.importFromFile(any(), eq(factory), isNull()))
                .thenReturn(BulkImportWeatherResult.failureWithMsg(BulkImportWeatherResult.Outcome.IO_ERROR, "broken file"));

        final BulkImportWeatherResult result = controller.importFromFile(Path.of("dummy.csv"));

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.IO_ERROR, result.outcome());
    }

    @Test
    void importFromFileReturnsEmptyWhenReaderHasNoRecords() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final BulkWeatherDataController controller = new BulkWeatherDataController(authz, weatherDataService, factory);
        when(weatherDataService.importFromFile(any(), eq(factory), isNull()))
                .thenReturn(BulkImportWeatherResult.success(List.of()));

        final BulkImportWeatherResult result = controller.importFromFile(Path.of("dummy.csv"));

        assertTrue(result.isSuccess());
        assertTrue(result.imported().isEmpty());
    }

    @Test
    void importFromFileFailsWhenFormatIsUnsupported() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final WeatherDataService weatherDataService = mock(WeatherDataService.class);
        final BulkWeatherDataController controller = new BulkWeatherDataController(authz, weatherDataService, new WeatherDataBulkReaderFactory());
        when(weatherDataService.importFromFile(any(), any(), isNull()))
                .thenReturn(BulkImportWeatherResult.failure(BulkImportWeatherResult.Outcome.UNSUPPORTED_FORMAT));

        final BulkImportWeatherResult result = controller.importFromFile(Path.of("weather.xml"));

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.UNSUPPORTED_FORMAT, result.outcome());
    }

    @Test
    void defaultConstructorInitializesDependencies() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> {
            final BulkWeatherDataController controller = new BulkWeatherDataController();
            assertNotNull(controller);
        });
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties properties = (Properties) propertiesField.get(Application.settings());
            properties.setProperty("persistence.repositoryFactory", TestWeatherDataRepositoryFactory.class.getName());

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
