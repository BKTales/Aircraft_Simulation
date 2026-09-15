package eapli.aisafe.rcomp.loggingserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private String mockLog(String user, String eventType) {
        return "[UDP 127.0.0.1] 2026-06-03T22:50:00Z|" + user + "|192.168.1.1|1111|US78|" + eventType;
    }

    @Test
    void extractPathStripsQueryString() {
        assertEquals("/api/events", HttpServer.extractPath("GET /api/events?limit=10 HTTP/1.1"));
    }

    @Test
    void toJsonArrayEscapesValues() {
        final String json = HttpServer.toJsonArray(List.of("abc", "x\"y"));
        assertEquals("[\"abc\",\"x\\\"y\"]", json);
    }

    @Test
    void eventStoreTracksActiveUsersFromMessages() {
        LogEventStore.addEvent(mockLog("user-a", "LOGIN_OK"));
        LogEventStore.addEvent(mockLog("user-b", "LOGIN_OK"));
        LogEventStore.addEvent(mockLog("user-a", "LOGOUT"));

        final List<String> active = LogEventStore.snapshotActiveUsers();
        assertTrue(active.contains("user-b"));
        assertFalse(active.contains("user-a"));
    }

    @Test
    void eventStoreRecognizesDisconnect() {
        LogEventStore.addEvent(mockLog("user-xpto", "LOGIN_OK"));
        LogEventStore.addEvent(mockLog("user-xpto", "DISCONNECT"));
        final List<String> active = LogEventStore.snapshotActiveUsers();
        assertTrue(active.stream().noneMatch(u -> u.equals("user-xpto")));
    }

    @Test
    void eventStoreIgnoresLoginFail() {
        LogEventStore.addEvent(mockLog("user-fail", "LOGIN_FAIL"));
        final List<String> active = LogEventStore.snapshotActiveUsers();
        assertTrue(active.stream().noneMatch(u -> u.equals("user-fail")));
    }

    @Test
    void spaIndexContainsRootElement() {
        final var resource = StaticResources.find("/");
        assertTrue(resource.isPresent(), "dashboard index.html must be on classpath (run npm build via Maven)");
        final String html = new String(resource.get().body(), StandardCharsets.UTF_8);
        assertTrue(html.contains("id=\"root\""));
        assertTrue(html.contains("Remote Access Monitor"));
    }

    @Test
    void spaRoutesServeIndex() {
        assertTrue(StaticResources.find("/events").isPresent());
        assertTrue(StaticResources.find("/active-users").isPresent());
    }
}