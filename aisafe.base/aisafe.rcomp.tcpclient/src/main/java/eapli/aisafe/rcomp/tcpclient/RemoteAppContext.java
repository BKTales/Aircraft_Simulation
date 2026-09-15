package eapli.aisafe.rcomp.tcpclient;

import java.util.Objects;

/**
 * Holds the active TCP session and login state for remote console UIs.
 */
public final class RemoteAppContext {

    private static RemoteAppContext instance;

    private final TcpSession session;
    private String loggedInUser;
    private RemoteProfile profile;
    private boolean active = true;

    public RemoteAppContext(final TcpSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    public static void set(final RemoteAppContext context) {
        instance = context;
    }

    public static RemoteAppContext require() {
        if (instance == null) {
            throw new IllegalStateException("Remote app context is not initialized");
        }
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    public TcpSession session() {
        return session;
    }

    public String loggedInUser() {
        return loggedInUser;
    }

    public RemoteProfile profile() {
        return profile;
    }

    public boolean isActive() {
        return active;
    }

    public void setLoggedIn(final String username, final RemoteProfile profile) {
        this.loggedInUser = Objects.requireNonNull(username, "username");
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    public void endSession() {
        active = false;
    }
}
