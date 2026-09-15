package eapli.aisafe.rcomp.loggingserver;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class LogEventStore {
    private static final int MAX_EVENTS = 500;
    private static final Deque<String> EVENTS = new ArrayDeque<>();
    private static final Set<String> ACTIVE_USERS = new LinkedHashSet<>();

    private LogEventStore() {}

    static synchronized void addEvent(final String event) {
        final String normalized = event == null ? "" : event.trim();
        if (normalized.isEmpty()) return;

        EVENTS.addLast(Instant.now() + " | " + normalized);

        while (EVENTS.size() > MAX_EVENTS) {
            EVENTS.removeFirst();
        }
        updateActiveUsers(normalized);
    }

    static synchronized List<String> snapshotEvents() {
        return new ArrayList<>(EVENTS);
    }

    static synchronized List<String> snapshotActiveUsers() {
        return new ArrayList<>(ACTIVE_USERS);
    }

    private static void updateActiveUsers(final String message) {
        final String payload = extractPayload(message);
        final String[] parts = payload.split("\\|");

        if (parts.length == 6) {
            final String username  = parts[1].trim();
            final String eventType = parts[5].trim();
            if (username.isBlank()) return;

            switch (eventType) {
                case "LOGIN_OK"   -> ACTIVE_USERS.add(username);
                case "LOGOUT",
                     "DISCONNECT" -> ACTIVE_USERS.remove(username);
                default           -> { }
            }
        }
    }

    private static String extractPayload(final String message) {
        final int bracket = message.indexOf("] ");
        return bracket >= 0 ? message.substring(bracket + 2) : message;
    }
}