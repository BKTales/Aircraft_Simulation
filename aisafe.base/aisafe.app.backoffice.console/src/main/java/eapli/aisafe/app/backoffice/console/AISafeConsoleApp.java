package eapli.aisafe.app.backoffice.console;

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
 * Single entry point: shared login (no per-app role filter) and main menu loop.
 * After root Exit, returns to login without exiting the JVM (eCafeteria-style extension).
 */
@SuppressWarnings("squid:S106")
public final class AISafeConsoleApp extends AISafeBaseApplication {

    private AISafeConsoleApp() {
    }

    public static void main(final String[] args) {
        new AISafeConsoleApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        while (true) {
            ApplicationQuit.clear();
            AisafeConsoleBanner.printLogo();

            final LoginUI login = new LoginUI(new AuthenticationCredentialHandler());
            if (!login.show()) {
                return;
            }
            new MainMenu().mainLoop();
            if (ApplicationQuit.isRequested()) {
                return;
            }
        }
    }

    @Override
    protected void configureAuthz() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new NilPasswordPolicy(), new PlainTextEncoder());
    }

    @Override
    protected String appTitle() {
        return "AISafe Console";
    }

    @Override
    protected String appGoodbye() {
        return "AISafe Console";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        // none
    }
}
