package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.application.RegisterAirControlAreaController;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterAirControlAreaControllerTest {

    @Test
    public void testControllerInitializationAndAuth() {
        AuthorizationService authz = mock(AuthorizationService.class);
        AirControlAreaService service = mock(AirControlAreaService.class);

        RegisterAirControlAreaController controller = new RegisterAirControlAreaController(authz, service);

        List<float[]> coords = new ArrayList<>();
        coords.add(new float[]{0,0});
        coords.add(new float[]{10,0});
        coords.add(new float[]{0,10});

        controller.registerAirControlArea("Test Area", coords, 250.0f);

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                eapli.aisafe.usermanagement.domain.AISafeRoles.ADMIN,
                eapli.aisafe.usermanagement.domain.AISafeRoles.BACKOFFICE_OPERATOR
        );

        verify(service).registerNewArea(eq("Test Area"), anyList(), eq(250.0f));
    }

    @Test
    public void testConstructorExceptions() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final AirControlAreaService service = mock(AirControlAreaService.class);

        assertThrows(IllegalArgumentException.class, () -> new RegisterAirControlAreaController(null, service));
        assertThrows(IllegalArgumentException.class, () -> new RegisterAirControlAreaController(authz, null));
    }

    @Test
    public void availableAreasDelegatesToService() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final AirControlAreaService service = mock(AirControlAreaService.class);
        final List<Object> expected = List.of();
        when(service.availableAreas()).thenReturn((Iterable) expected);

        final RegisterAirControlAreaController controller = new RegisterAirControlAreaController(authz, service);
        assertSame(expected, controller.availableAreas());
        verify(service).availableAreas();
    }

    @Test
    public void defaultConstructorCoverage() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();
        assertDoesNotThrow(() -> {
            new RegisterAirControlAreaController();
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
