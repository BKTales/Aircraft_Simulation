package eapli.aisafe.usermanagement.application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.*;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActivateDeactivateUserControllerTest {

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
        }
    }

    private static class FakeUserManagementService extends UserManagementService {
        private final UserRepository repository;
        boolean deactivateCalled = false;
        boolean activateCalled = false;

        FakeUserManagementService(UserRepository repository) {
            super(repository, new NilPasswordPolicy(), new PlainTextEncoder());
            this.repository = repository;
        }

        @Override
        public Iterable<SystemUser> activeUsers() {
            return repository.findByActive(true);
        }

        @Override
        public Iterable<SystemUser> deactivatedUsers() {
            return repository.findByActive(false);
        }

        @Override
        public SystemUser deactivateUser(final SystemUser user) {
            deactivateCalled = true;
            user.deactivate(java.util.Calendar.getInstance());
            return repository.save(user);
        }

        @Override
        public SystemUser activateUser(final SystemUser user) {
            activateCalled = true;
            user.activate();
            return repository.save(user);
        }
    }

    private InMemoryUserRepository repository;
    private SystemUser testUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();

        testUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("testuser", "Password1", "Test", "User", "test@test.com")
                .withRoles(AISafeRoles.ADMIN)
                .build();
    }

    @Test
    void activeUsersReturnsUsersFromService() {
        repository.save(testUser);
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        final Iterable<SystemUser> users = controller.activeUsers();

        assertNotNull(users);
        final Iterator<SystemUser> it = users.iterator();
        assertTrue(it.hasNext());
    }

    @Test
    void nonActiveUsersReturnsUsersFromService() {
        repository.save(testUser);
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);
        controller.deactivateUser(testUser);

        final Iterable<SystemUser> users = controller.nonActiveUsers();

        assertNotNull(users);
        final Iterator<SystemUser> it = users.iterator();
        assertTrue(it.hasNext());
    }

//    @Test
//    void controllerRequiresInitialization() {
//        // Construtor vazio chama AuthzRegistry não configurado — explode no construtor.
//        assertThrows(IllegalStateException.class, () -> new ActivateDeactivateUserController());
//    }

    @Test
    void unauthorizedUserThrowsExceptionOnActiveUsers() {
        final var authz = new FakeAuthorizationService(true);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        assertThrows(IllegalStateException.class, () -> controller.activeUsers());
    }

    @Test
    void deactivateUserSucceeds() {
        repository.save(testUser);
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        final SystemUser result = controller.deactivateUser(testUser);

        assertNotNull(result);
        assertTrue(userSvc.deactivateCalled); // substitui o verify() do Mockito
        assertFalse(result.isActive());
    }

    @Test
    void unauthorizedUserThrowsExceptionOnDeactivateUser() {
        final var authz = new FakeAuthorizationService(true);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        assertThrows(IllegalStateException.class, () -> controller.deactivateUser(testUser));
    }

    @Test
    void activateUserSucceeds() {
        testUser.deactivate(java.util.Calendar.getInstance());
        repository.save(testUser);
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        final SystemUser result = controller.activateUser(testUser);

        assertNotNull(result);
        assertTrue(userSvc.activateCalled);
        assertTrue(result.isActive());
    }

    @Test
    void unauthorizedUserThrowsExceptionOnActivateUser() {
        final var authz = new FakeAuthorizationService(true);
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        assertThrows(IllegalStateException.class, () -> controller.activateUser(testUser));
    }

    @Test
    void currentUserReturnsAuthenticatedUser() {
        final var authz = new FakeAuthorizationService(false) {
            @Override
            public Optional<SystemUser> loggedinUserWithPermissions(final Role... actions) {
                return Optional.of(testUser);
            }
        };
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        final Optional<SystemUser> result = controller.currentUser();

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void currentUserReturnsEmptyWhenNoSession() {
        final var authz = new FakeAuthorizationService(false) {
            @Override
            public Optional<SystemUser> loggedinUserWithPermissions(final Role... actions) {
                return Optional.empty();
            }
        };
        final var userSvc = new FakeUserManagementService(repository);
        final var controller = new ActivateDeactivateUserController(authz, userSvc);

        final Optional<SystemUser> result = controller.currentUser();

        assertFalse(result.isPresent());
    }

    @Test
    void constructorThrowsExceptionWhenAuthzIsNull() {
        final var userSvc = new FakeUserManagementService(repository);

        assertThrows(IllegalArgumentException.class, () ->
                new ActivateDeactivateUserController(null, userSvc));
    }

    @Test
    void constructorThrowsExceptionWhenUserSvcIsNull() {
        final var authz = new FakeAuthorizationService(false);

        assertThrows(IllegalArgumentException.class, () ->
                new ActivateDeactivateUserController(authz, null));
    }
}