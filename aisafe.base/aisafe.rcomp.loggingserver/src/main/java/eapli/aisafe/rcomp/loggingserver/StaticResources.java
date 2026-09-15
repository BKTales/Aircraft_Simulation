package eapli.aisafe.rcomp.loggingserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

final class StaticResources {

    private static final String DASHBOARD_PREFIX = "/dashboard/";

    private StaticResources() {}

    static Optional<Resource> find(final String path) {
        if (path == null || path.isBlank()) {
            return Optional.empty();
        }
        if (isSpaRoute(path)) {
            return load("index.html");
        }
        if (path.startsWith("/assets/")) {
            return load(path.substring(1));
        }
        if (path.equals("/favicon.ico")) {
            return load("favicon.ico");
        }
        return Optional.empty();
    }

    static boolean isSpaRoute(final String path) {
        return "/".equals(path) || "/events".equals(path) || "/active-users".equals(path);
    }

    static String contentType(final String classpathName) {
        final String lower = classpathName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (lower.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (lower.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (lower.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        return "application/octet-stream";
    }

    private static Optional<Resource> load(final String relativePath) {
        final String resourcePath = DASHBOARD_PREFIX + relativePath;
        try (InputStream in = StaticResources.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return Optional.empty();
            }
            final byte[] bytes = readAllBytes(in);
            return Optional.of(new Resource(bytes, contentType(relativePath)));
        } catch (final IOException ex) {
            return Optional.empty();
        }
    }

    private static byte[] readAllBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    record Resource(byte[] body, String contentType) {}
}
