package eapli.aisafe.app.common.console.presentation.authz;

import eapli.aisafe.app.common.console.ApplicationQuit;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Ends the console session and asks the host {@code doMain} loop to terminate the JVM run cleanly.
 */
public final class QuitApplicationAction implements Action {

    @Override
    public boolean execute() {
        AuthzRegistry.authorizationService().clearSession();
        ApplicationQuit.request();
        return true;
    }
}
