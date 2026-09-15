package eapli.aisafe.rcomp.tcpclient;

import eapli.aisafe.rcomp.tcpclient.presentation.RemoteLoginUI;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteCollaboratorMenuUI;

/**
 * AISafe Remote App — TCP client for US078 (ATCC) and US086 (Pilot).
 * Console UI uses eapli {@code Menu}, {@code AbstractUI}, and {@code SelectWidget}.
 */
public final class RemoteClientApp {

    private RemoteClientApp() {}

    public static void main(final String[] args) throws Exception {
        final String host = System.getProperty("AISAFE_RCOMP_HOST", "vsgate-s2.dei.isep.ipp.pt");
        final int port = Integer.parseInt(System.getProperty("AISAFE_RCOMP_TCP_PORT", "10353"));
        RemoteAppConsole.printBanner(null);
        try (TcpSession tcp = new TcpSession(host, port)) {
            final RemoteAppContext context = new RemoteAppContext(tcp);
            RemoteAppContext.set(context);
            System.out.println("Server: " + host + ":" + port);
            System.out.println();
            if (!new RemoteLoginUI().show()) {
                return;
            }
            RemoteAppConsole.printBanner(
                    "@" + context.loggedInUser() + " [" + context.profile().loginToken() + "]");
            while (context.isActive()) {
                new RemoteCollaboratorMenuUI().show();
            }
        } finally {
            RemoteAppContext.clear();
        }
    }
}
