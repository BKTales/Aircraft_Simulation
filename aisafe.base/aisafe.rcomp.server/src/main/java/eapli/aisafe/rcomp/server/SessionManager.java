package eapli.aisafe.rcomp.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static final Set<String> activeSessions = Collections.synchronizedSet(new HashSet<>());

    public static boolean login(String username) {
        return activeSessions.add(username);
    }

    public static void logout(String username) {
        if (username != null) {
            activeSessions.remove(username);
        }
    }
}