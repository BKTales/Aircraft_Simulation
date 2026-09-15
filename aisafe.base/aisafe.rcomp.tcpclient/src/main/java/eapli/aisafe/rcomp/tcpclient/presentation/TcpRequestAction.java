package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.framework.actions.Action;

import java.io.IOException;

public final class TcpRequestAction implements Action {

    private final int opcode;
    private final String payload;

    public TcpRequestAction(final int opcode, final String payload) {
        this.opcode = opcode;
        this.payload = payload == null ? "" : payload;
    }

    @Override
    public boolean execute() {
        try {
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(opcode, payload));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }
}
