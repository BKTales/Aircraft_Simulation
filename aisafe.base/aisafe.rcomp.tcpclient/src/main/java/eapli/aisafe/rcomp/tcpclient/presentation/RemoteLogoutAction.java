package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.rcomp.protocol.CommonOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.tcpclient.RemoteAppContext;
import eapli.aisafe.rcomp.tcpclient.RemoteAppConsole;
import eapli.framework.actions.Action;

import java.io.IOException;

@SuppressWarnings("squid:S106")
public final class RemoteLogoutAction implements Action {

    @Override
    public boolean execute() {
        try {
            final ProtocolFrame resp = RemoteTcpGateway.request(CommonOpcodes.LOGOUT, "");
            if (resp != null && resp.opcode() != CommonOpcodes.SUCCESS_LOGOUT) {
                System.out.println(RemoteTcpGateway.messageFrom(resp, "Logout failed."));
            }
        } catch (final IOException ex) {
            // Server may close the socket right after logout.
        }
        RemoteAppContext.require().endSession();
        RemoteAppConsole.clear();
        System.out.println("Logged out.");
        return false;
    }
}
