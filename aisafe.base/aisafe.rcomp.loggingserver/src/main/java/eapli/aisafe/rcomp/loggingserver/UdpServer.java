package eapli.aisafe.rcomp.loggingserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("squid:S106")
public final class UdpServer {

    private UdpServer() {}

    public static void boot(final int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("RCOMP UDP server listening on port " + port);
            // #region agent log
            DebugNdjsonLogger.log("initial", "H1", "UdpServer.boot",
                    "UDP server socket created", "{\"listenPort\":" + port + "}");
            // #endregion

            final byte[] buffer = new byte[2048];
            while (true) {
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                final String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                final String clientIP = packet.getAddress().getHostAddress();
                final int clientPort = packet.getPort();
                final String event = "[UDP " + clientIP + "] " + message;
                LogEventStore.addEvent(event);
                // #region agent log
                DebugNdjsonLogger.log("initial", "H4", "UdpServer.boot",
                        "UDP packet received and stored",
                        "{\"clientIp\":\"" + clientIP + "\",\"clientPort\":" + clientPort + ",\"bytes\":" + packet.getLength() + "}");
                // #endregion
                System.out.println(event + " (srcPort=" + clientPort + ", bytes=" + packet.getLength() + ")");
            }
        } catch (final Exception e) {
            // #region agent log
            DebugNdjsonLogger.log("initial", "H1", "UdpServer.boot",
                    "UDP server failed while binding/receiving",
                    "{\"error\":\"" + e.getMessage().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
            // #endregion
            System.err.println("ERROR UDP: " + e.getMessage());
        }
    }
}

