package eapli.aisafe.rcomp.server;


import java.time.Instant;

/**
 * Builds and dispatches structured log events to the Remote Accesses Logging Server (US90).
 */
public final class RemoteAccessLogger {

    private RemoteAccessLogger() {}

    public enum Event { LOGIN_OK, LOGIN_FAIL, LOGOUT, DISCONNECT }

    public static void log(final Event event,
                           final String username,
                           final String clientIp,
                           final int clientPort,
                           final String accessService,
                           final String operation) {

        String eventOrOp = (event != null) ? event.name() : operation;

        final String payload = String.join("|",
                Instant.now().toString(),
                username == null ? "unknown" : username,
                clientIp,
                String.valueOf(clientPort),
                accessService,
                eventOrOp
        );

        UdpClient.send(payload);
    }
}