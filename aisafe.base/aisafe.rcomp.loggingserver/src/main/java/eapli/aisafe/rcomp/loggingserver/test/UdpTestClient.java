package eapli.aisafe.rcomp.loggingserver.test;

import eapli.aisafe.rcomp.loggingserver.DebugNdjsonLogger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("squid:S106")
public final class UdpTestClient {
    public static void main(final String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            final String logMessage = "EVENT: USER XPTO LOGGED IN.";
            final byte[] data = logMessage.getBytes(StandardCharsets.UTF_8);
            final String host = System.getProperty(
                    "AISAFE_RCOMP_LOG_UDP_GATE_HOST",
                    System.getProperty("AISAFE_RCOMP_LOG_HOST", "vsgate-s3.dei.isep.ipp.pt"));
            final int port = Integer.parseInt(System.getProperty(
                    "AISAFE_RCOMP_LOG_UDP_GATE_PORT",
                    System.getProperty("AISAFE_RCOMP_LOG_UDP_PORT", "10387")));
            final InetAddress serverAddress = InetAddress.getByName(host);
            // #region agent log
            DebugNdjsonLogger.log("initial", "H2", "UdpTestClient.main",
                    "Resolved UDP destination",
                    "{\"host\":\"" + host.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"port\":" + port + "}");
            // #endregion
            final DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);
            socket.send(packet);
            // #region agent log
            DebugNdjsonLogger.log("initial", "H3", "UdpTestClient.main",
                    "UDP send completed",
                    "{\"bytes\":" + data.length + ",\"targetIp\":\""
                            + serverAddress.getHostAddress().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
            // #endregion
            System.out.println("LOG SENT VIA UDP!");
        } catch (final Exception e) {
            // #region agent log
            DebugNdjsonLogger.log("initial", "H3", "UdpTestClient.main",
                    "UDP client failed while sending",
                    "{\"error\":\"" + e.getMessage().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
            // #endregion
            System.err.println("ERROR UDP: " + e.getMessage());
        }
    }
}

