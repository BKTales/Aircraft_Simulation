package eapli.aisafe.app.common.console;

/**
 * Signals that the user chose "Quit application" from the root menu (read by {@code doMain} after {@code mainLoop}).
 */
public final class ApplicationQuit {

    private static volatile boolean requested;

    private ApplicationQuit() {
    }

    public static void request() {
        requested = true;
    }

    public static void clear() {
        requested = false;
    }

    public static boolean isRequested() {
        return requested;
    }
}
