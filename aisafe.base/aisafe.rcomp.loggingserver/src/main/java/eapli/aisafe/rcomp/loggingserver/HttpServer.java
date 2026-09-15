package eapli.aisafe.rcomp.loggingserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class HttpServer {

    private HttpServer() {}

    public static void boot(final int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("RCOMP HTTP server listening on port " + port);
            while (true) {
                final Socket client = server.accept();
                new Thread(() -> handle(client)).start();
            }
        } catch (final Exception e) {
            System.err.println("ERROR HTTP: " + e.getMessage());
        }
    }

    private static void handle(final Socket client) {
        try (client;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = client.getOutputStream()) {
            final String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                return;
            }
            System.out.println("[HTTP " + client.getInetAddress().getHostAddress() + "] " + requestLine);
            final String path = extractPath(requestLine);
            System.out.println("[HTTP] path=" + path);

            if ("/api/events".equals(path)) {
                final List<String> events = LogEventStore.snapshotEvents();
                System.out.println("[HTTP] /api/events -> " + events.size() + " events");
                writeTextResponse(out, "application/json; charset=utf-8", toJsonArray(events));
                return;
            }
            if ("/api/active-users".equals(path)) {
                final List<String> users = LogEventStore.snapshotActiveUsers();
                System.out.println("[HTTP] /api/active-users -> " + users.size() + " users");
                writeTextResponse(out, "application/json; charset=utf-8", toJsonArray(users));
                return;
            }

            final var resource = StaticResources.find(path);
            if (resource.isPresent()) {
                final StaticResources.Resource r = resource.get();
                System.out.println("[HTTP] static " + path + " (" + r.body().length + " bytes)");
                writeBytesResponse(out, r.contentType(), r.body());
                return;
            }

            System.out.println("[HTTP] 404 " + path);
            writeNotFound(out);
        } catch (final Exception ignored) {
            // connection closed or client aborted
        }
    }

    static String extractPath(final String requestLine) {
        final String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return "/";
        }
        final String rawPath = parts[1];
        final int queryIdx = rawPath.indexOf('?');
        return queryIdx >= 0 ? rawPath.substring(0, queryIdx) : rawPath;
    }

    static String toJsonArray(final List<String> values) {
        final StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escapeJson(values.get(i))).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escapeJson(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void writeTextResponse(final OutputStream out, final String contentType, final String body)
            throws Exception {
        writeBytesResponse(out, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeBytesResponse(final OutputStream out, final String contentType, final byte[] body)
            throws Exception {
        final String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private static void writeNotFound(final OutputStream out) throws Exception {
        final byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
        final String headers = "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type: text/plain; charset=utf-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
