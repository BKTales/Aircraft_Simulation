package eapli.aisafe.app.flightmanagement.console;

import eapli.aisafe.app.backoffice.console.presentation.MainMenu;
import eapli.aisafe.app.common.console.ApplicationQuit;
import eapli.aisafe.app.common.console.AisafeConsoleBanner;
import eapli.aisafe.app.common.console.AISafeBaseApplication;
import eapli.aisafe.app.common.console.presentation.authz.LoginUI;
import eapli.aisafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

/**
 * Legacy entry point: same auth and root menu as unified console (eCafeteria-style login).
 * Prefer {@link eapli.aisafe.app.backoffice.console.AISafeConsoleApp} or {@code ./run-aisafe.sh}.
 */
@SuppressWarnings("squid:S106")
public final class AISafeFlightManagement extends AISafeBaseApplication {

    private AISafeFlightManagement() {
    }

    public static void main(final String[] args) {
        new AISafeFlightManagement().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        ApplicationQuit.clear();
        AisafeConsoleBanner.printLogo();

        final LoginUI login = new LoginUI(new AuthenticationCredentialHandler());
        final boolean authenticated = login.show();
        if (!authenticated) {
            if (login.wasAbandonedByUser()) {
                return;
            }
            System.out.println("Authentication unsuccessful! Exiting.");
            System.exit(1);
        }
        System.out.println("User authenticated with success!");
        new MainMenu().mainLoop();
        if (ApplicationQuit.isRequested()) {
            return;
        }
        System.out.println("Bye");
    }

    @Override
    protected void configureAuthz() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new NilPasswordPolicy(), new PlainTextEncoder());
    }

    @Override
    protected String appTitle() {
        return "AISafe Flight Management";
    }

    @Override
    protected String appGoodbye() {
        return "AISafe Flight Management";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        // none
    }
}
