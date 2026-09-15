package eapli.aisafe.usermanagement.application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.*;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ListUsersControllerTest {

    /**
     * Fake de AuthorizationService que permite controlar
     * se lança exceção ou não no ensureAuthenticatedUserHasAnyOf.
     */
    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean shouldThrow;

        FakeAuthorizationService(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {
            if (shouldThrow) {
                throw new IllegalStateException("Unauthorized");
            }
            // caso contrário, não faz nada (utilizador autorizado)
        }
    }

    /**
     * Fake de UserManagementService que delega no repositório
     * em memória fornecido no construtor.
     */
    private static class FakeUserManagementService extends UserManagementService {
        private final UserRepository repository;

        FakeUserManagementService(UserRepository repository) {
            // UserManagementService precisa de dependências no construtor;
            // passamos null pois os métodos que usamos são sobrescritos.
            super(repository, new NilPasswordPolicy(), new PlainTextEncoder());
            this.repository = repository;
        }

        @Override
        public Iterable<SystemUser> allUsers() {
            return repository.findAll();
        }

        @Override
        public Optional<SystemUser> userOfIdentity(final Username id) {
            return repository.ofIdentity(id);
        }
    }

    private InMemoryUserRepository repository;
    private SystemUser adminUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();

        adminUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("admin", "Password1", "Admin", "User", "admin@test.com")
                .withRoles(AISafeRoles.ADMIN)
                .build();

        repository.save(adminUser);
    }

    @Test
    void allUsersReturnsUsersFromService() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ListUsersController(authz, userSvc);

        final Iterable<SystemUser> users = controller.allUsers();

        assertNotNull(users);
        final Iterator<SystemUser> it = users.iterator();
        assertTrue(it.hasNext());
    }

//    @Test
//    void controllerRequiresInitialization() {
//        assertThrows(IllegalStateException.class, () -> new ListUsersController());
//    }

    @Test
    void unauthorizedUserThrowsException() {
        final var authz = new FakeAuthorizationService(true); // lança sempre
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ListUsersController(authz, userSvc);

        assertThrows(IllegalStateException.class, () -> controller.allUsers());
    }

    @Test
    void authorizedUserCanListUsers() {
        final var authz = new FakeAuthorizationService(false); // nunca lança
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ListUsersController(authz, userSvc);

        final Iterable<SystemUser> users = controller.allUsers();

        assertNotNull(users);
    }

    @Test
    void findUserByUsername() {
        final SystemUser testUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("testuser", "Password1", "Test", "User", "test@test.com")
                .withRoles(AISafeRoles.ADMIN)
                .build();
        repository.save(testUser);

        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ListUsersController(authz, userSvc);

        final Username username = testUser.username();
        final Optional<SystemUser> result = controller.find(username);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findUserByUsernameNotFound() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);

        // Username que não existe no repositório
        final Username nonExistent = Username.valueOf("naoexiste");
        final var controller = new ListUsersController(authz, userSvc);

        final Optional<SystemUser> result = controller.find(nonExistent);

        assertFalse(result.isPresent());
    }

    @Test
    void constructorThrowsExceptionWhenAuthzIsNull() {
        final var userSvc = new FakeUserManagementService(repository);

        assertThrows(IllegalArgumentException.class, () ->
                new ListUsersController(null, userSvc));
    }

    @Test
    void constructorThrowsExceptionWhenUserSvcIsNull() {
        final var authz = new FakeAuthorizationService(false);

        assertThrows(IllegalArgumentException.class, () ->
                new ListUsersController(authz, null));
    }
}