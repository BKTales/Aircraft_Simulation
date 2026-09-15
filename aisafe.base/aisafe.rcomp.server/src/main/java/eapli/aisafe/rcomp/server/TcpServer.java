package eapli.aisafe.rcomp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("squid:S106")
public final class TcpServer {

    private TcpServer() {}

    public static void boot(final int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("RCOMP TCP server listening on port " + port);
            while (true) {
                final Socket clientSocket = socket.accept();
                System.out.println("New client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (final IOException e) {
            System.out.println("Error creating TCP server.\n" + e.getMessage());
        }
    }
}

