package eapli.aisafe.app.backoffice.console;

import eapli.aisafe.Application;
import eapli.aisafe.app.backoffice.console.presentation.MainMenu;
import eapli.aisafe.app.common.console.ApplicationQuit;
import eapli.aisafe.app.common.console.AisafeConsoleBanner;
import eapli.aisafe.app.common.console.AISafeBaseApplication;
import eapli.aisafe.app.common.console.presentation.authz.LoginUI;
import eapli.aisafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.aisafe.infrastructure.bootstrapers.Bootstrapper;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("squid:S106")
public final class AISafeBackoffice extends AISafeBaseApplication {

    private static final String IN_MEMORY_REPOSITORY_FACTORY =
            "eapli.aisafe.persistence.impl.inmemory.InMemoryRepositoryFactory";

    private static final Logger LOGGER = LogManager.getLogger(AISafeBackoffice.class);

    private AISafeBackoffice() {
    }

    public static void main(final String[] args) {
        new AISafeBackoffice().run(args);
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
    protected void configure() {
        configureAuthz();
        bootstrapInMemoryDataIfNeeded();
        configurePubSub();
    }

    @Override
    protected void configureAuthz() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new NilPasswordPolicy(), new PlainTextEncoder());
    }

    /**
     * In-memory repositories live only inside the current JVM. A separate Bootstrapper run
     * does not populate the console process, so seed demo data here before login.
     */
    private static void bootstrapInMemoryDataIfNeeded() {
        if (!IN_MEMORY_REPOSITORY_FACTORY.equals(Application.settings().getRepositoryFactory())) {
            return;
        }
        LOGGER.info("In-memory persistence: loading demo data (bootstrap)...");
        new Bootstrapper().execute();
    }

    @Override
    protected String appTitle() {
        return "AISafe Backoffice";
    }

    @Override
    protected String appGoodbye() {
        return "AISafe Backoffice";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        // none
    }
}
