package eapli.aisafe.usermanagement.application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.*;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddUserControllerTest {

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
        private final SystemUser userToReturn;
        boolean registerCalled = false;

        FakeUserManagementService(SystemUser userToReturn) {
            super(new InMemoryUserRepository(), new NilPasswordPolicy(), new PlainTextEncoder());
            this.userToReturn = userToReturn;
        }

        @Override
        public SystemUser registerNewUser(final String username, final String rawPassword,
                                          final String firstName, final String lastName, final String email,
                                          final Set<Role> roles, final Calendar createdOn) {
            registerCalled = true;
            return userToReturn;
        }
    }

    private static class FakeEmailDomainRepository implements EmailDomainRepository {
        private final boolean domainExists;

        FakeEmailDomainRepository(boolean domainExists) {
            this.domainExists = domainExists;
        }

        @Override
        public Optional<EmailDomain> findByDomain(String domain) {
            return domainExists ? Optional.of(new EmailDomain(domain)) : Optional.empty();
        }

        @Override public <S extends EmailDomain> S save(S entity) { return entity; }
        @Override public Optional<EmailDomain> ofIdentity(String id) { return Optional.empty(); }
        @Override public boolean containsOfIdentity(String id) { return false; }
        @Override public void delete(EmailDomain entity) {}
        @Override public void deleteOfIdentity(String id) {}
        @Override public long count() { return 0; }
        @Override public Iterable<EmailDomain> findAll() { return java.util.List.of(); }
    }

    private static class FakeAddUserService extends AddUserService {
        FakeAddUserService(boolean domainExists) {
            super(new FakeEmailDomainRepository(domainExists));
        }
    }

    private SystemUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("testuser", "Password1", "Test", "User", "test@aisafe.admin.com")
                .withRoles(AISafeRoles.ADMIN)
                .build();
    }

    @Test
    void getRoleTypesReturnsCorrectRoles() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(true);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final Role[] roles = controller.getRoleTypes();

        assertNotNull(roles);
        assertTrue(roles.length > 0);
        assertEquals(AISafeRoles.backofficeRegisterableValues().length, roles.length);
    }

    @Test
    void addUserWithValidDataSucceeds() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(true);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@aisafe.admin.com",
                Set.of(AISafeRoles.ADMIN));

        assertTrue(result.isSuccess());
        assertEquals(testUser, result.user().orElseThrow());
        assertTrue(userSvc.registerCalled);
    }

    @Test
    void addUserWithInvalidDomainReturnsFailure() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(false);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@invalid.com",
                Set.of(AISafeRoles.ADMIN));

        assertFalse(result.isSuccess());
        assertEquals(AddUserResult.Outcome.INVALID_EMAIL_DOMAIN, result.outcome());
        assertFalse(userSvc.registerCalled);
    }

    @Test
    void addUserWithPilotRoleReturnsFailure() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(false);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@qualquerdominio.com",
                Set.of(AISafeRoles.PILOT));

        assertFalse(result.isSuccess());
        assertEquals(AddUserResult.Outcome.ROLE_NOT_REGISTERABLE, result.outcome());
        assertFalse(userSvc.registerCalled);
    }

    @Test
    void addUserWithAirTransportCompanyCollaboratorRoleReturnsFailure() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(false);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@qualquerdominio.com",
                Set.of(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR));

        assertFalse(result.isSuccess());
        assertEquals(AddUserResult.Outcome.ROLE_NOT_REGISTERABLE, result.outcome());
        assertFalse(userSvc.registerCalled);
    }

    @Test
    void addUserWithFlightControlOperatorRoleReturnsFailure() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(false);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@qualquerdominio.com",
                Set.of(AISafeRoles.FLIGHT_CONTROL_OPERATOR));

        assertFalse(result.isSuccess());
        assertEquals(AddUserResult.Outcome.ROLE_NOT_REGISTERABLE, result.outcome());
        assertFalse(userSvc.registerCalled);
    }

    @Test
    void unauthorizedUserReturnsFailure() {
        final var authz = new FakeAuthorizationService(true);
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(true);
        final var controller = new AddUserController(authz, userSvc, addUserSvc);

        final AddUserResult result = controller.addUser(
                "testuser", "Password1", "Test", "User", "test@aisafe.admin.com",
                Set.of(AISafeRoles.ADMIN));

        assertFalse(result.isSuccess());
        assertEquals(AddUserResult.Outcome.UNAUTHORIZED, result.outcome());
        assertFalse(userSvc.registerCalled);
    }

    @Test
    void constructorThrowsExceptionWhenAuthzIsNull() {
        final var userSvc = new FakeUserManagementService(testUser);
        final var addUserSvc = new FakeAddUserService(true);

        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(null, userSvc, addUserSvc));
    }

    @Test
    void constructorThrowsExceptionWhenUserSvcIsNull() {
        final var authz = new FakeAuthorizationService(false);
        final var addUserSvc = new FakeAddUserService(true);

        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(authz, null, addUserSvc));
    }

    @Test
    void constructorThrowsExceptionWhenAddUserSvcIsNull() {
        final var authz = new FakeAuthorizationService(false);
        final var userSvc = new FakeUserManagementService(testUser);

        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(authz, userSvc, null));
    }
}
