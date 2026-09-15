package eapli.aisafe.usermanagement.application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Optional;

@UseCaseController
public class ActivateDeactivateUserController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;

    public ActivateDeactivateUserController() {
        this(AuthzRegistry.authorizationService(), AuthzRegistry.userService());
    }

    public ActivateDeactivateUserController(final AuthorizationService authz, final UserManagementService userSvc) {
        if(authz == null || userSvc == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }
        this.authz = authz;
        this.userSvc = userSvc;
    }

    public Iterable<SystemUser> activeUsers() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN);

        return userSvc.activeUsers();
    }

    public Iterable<SystemUser> nonActiveUsers() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN);

        return userSvc.deactivatedUsers();
    }

    public SystemUser activateUser(final SystemUser user) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN);

        return userSvc.activateUser(user);
    }

    public SystemUser deactivateUser(final SystemUser user) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN);

        return userSvc.deactivateUser(user);
    }

    public Optional<SystemUser> currentUser() {
        return authz.loggedinUserWithPermissions(AISafeRoles.ADMIN);
    }
}
