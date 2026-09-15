package eapli.aisafe.rcomp.tcpclient.test;

import eapli.aisafe.rcomp.protocol.CommonOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.tcpclient.TcpSession;

import java.util.Scanner;

@SuppressWarnings("squid:S106")
public final class TcpTestClient {
    public static void main(final String[] args) throws Exception {
        final String host = System.getProperty("AISAFE_RCOMP_HOST", "vsgate-s2.dei.isep.ipp.pt");
        final int port = Integer.parseInt(System.getProperty("AISAFE_RCOMP_TCP_PORT", "10353"));
        final Scanner scanner = new Scanner(System.in);
        System.out.println("=== AISafe ATCC login test ===");
        System.out.print("Username: ");
        final String username = scanner.nextLine();
        System.out.print("Password: ");
        final String password = scanner.nextLine();
        try (TcpSession session = new TcpSession(host, port)) {
            final ProtocolFrame resp = session.request(CommonOpcodes.LOGIN, username + ";" + password);
            System.out.println("code=" + resp.opcode() + " msg=" + resp.payload());
        }
    }
}

