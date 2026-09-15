package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.domain.FuelType;
import eapli.aisafe.enginemodelmanagement.domain.MotorizationType;
import eapli.aisafe.enginemodelmanagement.domain.TSFC;
import eapli.aisafe.enginemodelmanagement.domain.ThrustProfile;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.Application;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class CreateEngineModelControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean shouldThrow;
        Role[] lastRoles;

        FakeAuthorizationService(final boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            this.lastRoles = roles;
            if (shouldThrow) throw new IllegalStateException("Unauthorized");
        }
    }

    private static class FakeEngineModelService extends EngineModelService {
        int calls;
        Object[] lastArgs;
        EngineModel result;
        RuntimeException toThrow;

        FakeEngineModelService() {
            super(new DummyEngineModelRepository(), new DummyManufacturerRepository());
        }

        @Override
        public EngineModel createEngineModel(String name, String manufacturerId, String motorization,
                                             double thrustAtStatic, double thrustAtCruise,
                                             String fuelType, double tsfc) {
            calls++;
            lastArgs = new Object[]{name, manufacturerId, motorization, thrustAtStatic, thrustAtCruise, fuelType, tsfc};
            if (toThrow != null) throw toThrow;
            return result;
        }
    }

    private static class DummyEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {
        @Override
        public Optional<EngineModel> findByModelId(final EngineModelId id) {
            return Optional.ofNullable(data().get(id));
        }

        @Override
        public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
            return Optional.empty();
        }
    }

    private static class DummyManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
            implements ManufacturerRepository {
        @Override
        public Optional<Manufacturer> findById(final ManufacturerId id) {
            return Optional.empty();
        }
    }

    @Test
    void createEngineModelEnsuresAuthorizationAndDelegatesToService() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeEngineModelService service = new FakeEngineModelService();
        final CreateEngineModelController controller = new CreateEngineModelController(authz, service);

        final EngineModel expected = new EngineModel(
                EngineModelId.valueOf("E1"),
                EngineName.valueOf("N"),
                TSFC.valueOf(0.5),
                FuelType.JET_A1,
                ThrustProfile.valueOf(100, 90),
                new Manufacturer(
                        ManufacturerId.valueOf("MAN01"),
                        new ManufacturerName("Acme"),
                        new CountryCode("PT")),
                MotorizationType.TURBOFAN
        );
        service.result = expected;

        final EngineModel result = controller.createEngineModel("N", "MAN01", "turbofan", 100, 90, "JET_A1", 0.5);

        assertSame(expected, result);
        assertNotNull(authz.lastRoles);
        assertEquals(2, authz.lastRoles.length);
        assertSame(AISafeRoles.ADMIN, authz.lastRoles[0]);
        assertSame(AISafeRoles.BACKOFFICE_OPERATOR, authz.lastRoles[1]);
        assertEquals(1, service.calls);
        assertEquals("N", service.lastArgs[0]);
        assertEquals("MAN01", service.lastArgs[1]);
        assertEquals("turbofan", service.lastArgs[2]);
        assertEquals(100.0, (double) service.lastArgs[3], 0.000001);
        assertEquals(90.0, (double) service.lastArgs[4], 0.000001);
        assertEquals("JET_A1", service.lastArgs[5]);
        assertEquals(0.5, (double) service.lastArgs[6], 0.000001);
    }

    @Test
    void createEngineModelFailsWhenUserIsUnauthorized() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(true);
        final FakeEngineModelService service = new FakeEngineModelService();
        final CreateEngineModelController controller = new CreateEngineModelController(authz, service);

        assertThrows(IllegalStateException.class,
                () -> controller.createEngineModel("N", "MAN01", "turbofan", 100, 90, "JET_A1", 0.5));

        assertEquals(0, service.calls);
        assertNotNull(authz.lastRoles);
        assertEquals(2, authz.lastRoles.length);
    }

    @Test
    void constructorNullGuards() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeEngineModelService service = new FakeEngineModelService();

        final IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> new CreateEngineModelController(null, service));
        assertTrue(ex1.getMessage().contains("Authorization service"));

        final IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> new CreateEngineModelController(authz, null));
        assertTrue(ex2.getMessage().contains("engine model service"));
    }

    @Test
    void createEngineModelPropagatesServiceExceptionAfterAuthorization() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeEngineModelService service = new FakeEngineModelService();
        service.toThrow = new EngineModelAlreadyExistsException("dup");
        final CreateEngineModelController controller = new CreateEngineModelController(authz, service);

        assertThrows(EngineModelAlreadyExistsException.class,
                () -> controller.createEngineModel("N2", "MAN02", "turbojet", 10, 10, "JET_B", 0.6));

        assertNotNull(authz.lastRoles);
        assertEquals(1, service.calls);
        assertEquals("N2", service.lastArgs[0]);
    }

    @Test
    void createEngineModelDoesNotCallServiceWhenUnauthorized() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(true);
        final FakeEngineModelService service = new FakeEngineModelService();
        service.toThrow = new IllegalStateException("should not run");
        final CreateEngineModelController controller = new CreateEngineModelController(authz, service);

        assertThrows(IllegalStateException.class,
                () -> controller.createEngineModel("N3", "MAN03", "ramjet", 11, 11, "JET_A", 0.7));

        assertEquals(0, service.calls);
        assertNull(service.lastArgs);
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> new CreateEngineModelController());
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
            final Properties props = (Properties) propertiesField.get(Application.settings());
            props.setProperty("persistence.repositoryFactory", "eapli.aisafe.enginemodelmanagement.TestEngineModelRepositoryFactory");

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

