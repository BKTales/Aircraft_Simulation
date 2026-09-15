package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.RemoteAppContext;

import java.io.IOException;

@SuppressWarnings("squid:S106")
public final class RemoteTcpGateway {

    private RemoteTcpGateway() {}

    public static ProtocolFrame request(final int opcode, final String payload) throws IOException {
        return RemoteAppContext.require().session().request((byte) opcode, payload);
    }

    public static void printResponse(final ProtocolFrame resp) {
        if (resp == null) {
            System.out.println("No response from server.");
            return;
        }
        if (resp.opcode() == ResponseCodes.OK) {
            if (!resp.payload().isBlank()) {
                System.out.println(resp.payload());
            }
            return;
        }
        System.out.println(messageFrom(resp, "Request failed."));
    }

    public static String messageFrom(final ProtocolFrame resp, final String fallback) {
        if (resp == null) {
            return fallback;
        }
        return resp.payload().isBlank() ? fallback : resp.payload();
    }
}
