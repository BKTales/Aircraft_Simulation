package eapli.aisafe.infrastructure.bootstrapers;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.aisafe.usermanagement.application.AddUserController;
import eapli.aisafe.usermanagement.application.AddUserResult;
import eapli.aisafe.usermanagement.application.ListUsersController;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;

public class AbstractUserBootstrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserBootstrapper.class);

    final AddUserController userController = new AddUserController();
    final ListUsersController listUserController = new ListUsersController();

    protected SystemUser registerUser(final String username, final String password, final String firstName,
                                      final String lastName, final String email, final Set<Role> roles) {
        final AddUserResult result = userController.addUser(username, password, firstName, lastName, email, roles);

        if (result.isSuccess()) {
            final SystemUser user = result.user().orElseThrow(
                    () -> new IllegalStateException("Successful registration returned no user: " + username));
            LOGGER.debug("Created user {}", username);
            return user;
        }

        if (result.outcome() == AddUserResult.Outcome.DUPLICATE_USER) {
            return listUserController.find(Username.valueOf(username))
                    .orElseThrow(() -> new IllegalStateException("User not found after duplicate registration: " + username));
        }

        throw new IllegalStateException("Failed to register user " + username + ": " + result.message());
    }
}
