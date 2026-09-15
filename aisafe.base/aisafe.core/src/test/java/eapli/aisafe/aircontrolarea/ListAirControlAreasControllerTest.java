package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.ListAirControlAreasController;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListAirControlAreasControllerTest {

    @Test
    void allAirControlAreasChecksAuthAndReturnsRepositoryData() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);
        final Iterable<AirControlArea> expected = List.of();
        when(repository.findAll()).thenReturn(expected);

        final ListAirControlAreasController controller = new ListAirControlAreasController(authz, repository);

        assertSame(expected, controller.allAirControlAreas());
        verify(authz).ensureAuthenticatedUserHasAnyOf(any(), any(), any());
        verify(repository).findAll();
    }

    @Test
    void constructorNullGuards() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);

        assertThrows(IllegalArgumentException.class, () -> new ListAirControlAreasController(null, repository));
        assertThrows(IllegalArgumentException.class, () -> new ListAirControlAreasController(authz, null));
    }

    @Test
    void defaultConstructorCoverage() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();
        assertDoesNotThrow(() -> {
            new ListAirControlAreasController();
        });
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = eapli.aisafe.Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties props = (Properties) propertiesField.get(eapli.aisafe.Application.settings());
            props.setProperty("persistence.repositoryFactory", "eapli.aisafe.weatherdata.TestWeatherDataRepositoryFactory");

            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
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
        }
    }

    private void resetPersistenceFactory() {
        try {
            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
