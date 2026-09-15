package eapli.aisafe.rcomp.tcpclient;

/**
 * @deprecated Use {@link RemoteClientApp} for ATCC (US078) and Pilot (US086) remote access.
 */
@Deprecated
public final class RcompTcpClientApp {

    private RcompTcpClientApp() {}

    public static void main(final String[] args) throws Exception {
        RemoteClientApp.main(args);
    }
}
