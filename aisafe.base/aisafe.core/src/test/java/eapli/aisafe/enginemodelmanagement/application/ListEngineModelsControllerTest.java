package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.Application;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ListEngineModelsControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean shouldThrow;

        FakeAuthorizationService(final boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            if (shouldThrow) throw new IllegalStateException("Unauthorized");
        }
    }

    private static class FakeEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {

        int findAllCalls;

        @Override
        public Iterable<EngineModel> findAll() {
            findAllCalls++;
            return super.findAll();
        }

        @Override
        public Optional<EngineModel> findByModelId(final EngineModelId id) {
            return Optional.ofNullable(data().get(id));
        }

        @Override
        public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
            return Optional.empty();
        }
    }

    @Test
    void allEngineModelsEnsuresAuthorizationAndDelegatesToRepository() {
        final AuthorizationService authz = new FakeAuthorizationService(false);
        final FakeEngineModelRepository repo = new FakeEngineModelRepository();
        final ListEngineModelsController controller = new ListEngineModelsController(authz, repo);

        final var result = controller.allEngineModels();

        assertNotNull(result);
        assertEquals(1, repo.findAllCalls);
    }

    @Test
    void allEngineModelsFailsWhenUnauthorized() {
        final AuthorizationService authz = new FakeAuthorizationService(true);
        final FakeEngineModelRepository repo = new FakeEngineModelRepository();
        final ListEngineModelsController controller = new ListEngineModelsController(authz, repo);

        assertThrows(IllegalStateException.class, controller::allEngineModels);
        assertEquals(0, repo.findAllCalls);
    }

    @Test
    void constructorNullGuards() {
        final AuthorizationService authz = new FakeAuthorizationService(false);
        final EngineModelRepository repo = new FakeEngineModelRepository();

        assertThrows(IllegalArgumentException.class, () -> new ListEngineModelsController(null, repo));
        assertThrows(IllegalArgumentException.class, () -> new ListEngineModelsController(authz, null));
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> new ListEngineModelsController());
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

