package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.Application;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AddEngineModelToAircraftModelControllerTest {

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

    private static class FakeAircraftModelService extends AircraftModelService {
        int calls;
        Object[] lastArgs;
        AircraftModel result;
        RuntimeException toThrow;

        FakeAircraftModelService() {
            super(new DummyAircraftModelRepository(), new DummyManufacturerRepository(), new DummyEngineModelRepository());
        }

        @Override
        public AircraftModel addEngineModelToAircraftModel(final String aircraftModelId,
                                                           final String engineModelId) {
            calls++;
            lastArgs = new Object[]{aircraftModelId, engineModelId};
            if (toThrow != null) throw toThrow;
            return result;
        }
    }

    private static class DummyAircraftModelRepository extends InMemoryDomainRepository<AircraftModel, AircraftModelId>
            implements AircraftModelRepository {
        @Override
        public Optional<AircraftModel> findByID(final AircraftModelId id) { return Optional.empty(); }

        @Override
        public Optional<AircraftModel> existsByNameAndManufacturer(final ModelName name, final ManufacturerId manufacturerId) {
            return Optional.empty();
        }

        @Override
        public Iterable<AircraftModel> findByManufacturer(final ManufacturerId manufacturerId) { return List.of(); }
    }

    private static class DummyManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
            implements ManufacturerRepository {
        @Override
        public Optional<Manufacturer> findById(final ManufacturerId id) { return Optional.empty(); }
    }

    private static class DummyEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {
        @Override
        public Optional<EngineModel> findByModelId(final EngineModelId id) { return Optional.empty(); }

        @Override
        public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
            return Optional.empty();
        }
    }

    @Test
    void addEngineModelToAircraftModelEnsuresAuthorizationAndDelegatesToService() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeAircraftModelService service = new FakeAircraftModelService();
        final AddEngineModelToAircraftModelController controller = new AddEngineModelToAircraftModelController(authz, service);

        service.result = mock(AircraftModel.class);

        final AircraftModel result = controller.addEngineModelToAircraftModel("A320", "MAN01-CFM56");

        assertSame(service.result, result);
        assertNotNull(authz.lastRoles);
        assertEquals(2, authz.lastRoles.length);
        assertEquals(1, service.calls);
        assertEquals("A320", service.lastArgs[0]);
        assertEquals("MAN01-CFM56", service.lastArgs[1]);
    }

    @Test
    void addEngineModelToAircraftModelFailsWhenUnauthorized() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(true);
        final FakeAircraftModelService service = new FakeAircraftModelService();
        final AddEngineModelToAircraftModelController controller = new AddEngineModelToAircraftModelController(authz, service);

        assertThrows(IllegalStateException.class,
                () -> controller.addEngineModelToAircraftModel("A320", "MAN01-CFM56"));

        assertEquals(0, service.calls);
    }

    @Test
    void constructorNullGuards() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeAircraftModelService service = new FakeAircraftModelService();

        assertThrows(IllegalArgumentException.class,
                () -> new AddEngineModelToAircraftModelController(null, service));
        assertThrows(IllegalArgumentException.class,
                () -> new AddEngineModelToAircraftModelController(authz, null));
    }

    @Test
    void addEngineModelToAircraftModelPropagatesServiceException() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);
        final FakeAircraftModelService service = new FakeAircraftModelService();
        service.toThrow = new EngineModelNotFoundException("missing");

        final AddEngineModelToAircraftModelController controller = new AddEngineModelToAircraftModelController(authz, service);

        assertThrows(EngineModelNotFoundException.class,
                () -> controller.addEngineModelToAircraftModel("A320", "MAN01-CFM56"));
        assertEquals(1, service.calls);
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> new AddEngineModelToAircraftModelController());
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
            props.setProperty("persistence.repositoryFactory", "eapli.aisafe.aircraftmodelmanagement.TestAircraftModelRepositoryFactory");

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
