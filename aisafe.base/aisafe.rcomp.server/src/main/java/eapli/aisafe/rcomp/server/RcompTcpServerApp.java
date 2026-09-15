package eapli.aisafe.rcomp.server;

import eapli.aisafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;

/**
 * RCOMP TCP server (Cloud A). US078 ATCC and US086 Pilot commands handled by {@link ClientHandler}.
 */
@SuppressWarnings("squid:S106")
public final class RcompTcpServerApp {

    private RcompTcpServerApp() {}

    public static void main(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new NilPasswordPolicy(), new PlainTextEncoder());
        // Force AuthzRegistry init so authentication works in handler.
        new AuthenticationCredentialHandler();

        final int port = Integer.parseInt(System.getProperty("AISAFE_RCOMP_TCP_PORT", "2225"));
        TcpServer.boot(port);
    }
}

