package eapli.aisafe.rcomp.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public final class UdpClient {

    private static final String LOG_HOST = System.getProperty(
            "AISAFE_RCOMP_LOG_UDP_GATE_HOST", System.getProperty(
                    "AISAFE_RCOMP_LOG_UDP_HOST", System.getProperty(
                            "AISAFE_RCOMP_LOG_HOST", "vs387.dei.isep.ipp.pt")));

    private static final int LOG_PORT = Integer.parseInt(System.getProperty(
            "AISAFE_RCOMP_LOG_UDP_GATE_PORT", System.getProperty(
                    "AISAFE_RCOMP_LOG_UDP_PORT", "2227")));
    private UdpClient() {}

    /**
     * Sends a single UTF-8 datagram to the logging server.
     * Fire-and-forget — never throws; logs locally on failure.
     */
    public static void send(final String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            final byte[] data = message.getBytes(StandardCharsets.UTF_8);
            final DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(LOG_HOST), LOG_PORT);
            socket.send(packet);
        } catch (final Exception e) {
            System.err.println("WARN: UDP log send failed: " + e.getMessage());
        }
    }
}