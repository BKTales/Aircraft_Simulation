package eapli.aisafe.rcomp.loggingserver;

/**
 * RCOMP Sprint 3 skeleton: UDP receiver + HTTP server (US90/US91).
 * This is only the bootstrap; endpoints and storage will be added next.
 */
@SuppressWarnings("squid:S106")
public final class LoggingServerApp {

    private LoggingServerApp() {}

    public static void main(final String[] args) {
        final int udpPort = Integer.parseInt(System.getProperty("AISAFE_RCOMP_UDP_PORT", "2227"));
        final int httpPort = Integer.parseInt(System.getProperty("AISAFE_RCOMP_HTTP_PORT", "2224"));
        // #region agent log
        DebugNdjsonLogger.log("initial", "H5", "LoggingServerApp.main",
                "Logging server boot parameters",
                "{\"udpPort\":" + udpPort + ",\"httpPort\":" + httpPort + "}");
        // #endregion

        new Thread(() -> UdpServer.boot(udpPort)).start();
        new Thread(() -> HttpServer.boot(httpPort)).start();
    }
}

