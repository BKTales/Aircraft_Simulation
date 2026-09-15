package eapli.aisafe.rcomp.tcpclient;

/**
 * Terminal UI helpers for AISafe Remote App (clear screen + banner, like the main console).
 */
@SuppressWarnings("squid:S106")
public final class RemoteAppConsole {

    static final String SEPARATOR = "============================================";
    static final String TITLE = "AISafe Remote App — Air Transport Company (ATCC / Pilot)";

    private RemoteAppConsole() {}

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printBanner(final String sessionLine) {
        clear();
        printLogo();
        System.out.println(SEPARATOR);
        System.out.println(TITLE);
        if (sessionLine != null && !sessionLine.isBlank()) {
            System.out.println(sessionLine);
        }
        System.out.println(SEPARATOR);
        System.out.println();
    }

    private static void printLogo() {
        System.out.println(
                """
                         ______     __     ______     ______     ______   ______   \s
                        /\\  __ \\   /\\ \\   /\\  ___\\   /\\  __ \\   /\\  ___\\ /\\  ___\\  \s
                        \\ \\  __ \\  \\ \\ \\  \\ \\___  \\  \\ \\  __ \\  \\ \\  __\\ \\ \\  __\\  \s
                         \\ \\_\\ \\_\\  \\ \\_\\  \\/\\_____\\  \\ \\_\\ \\_\\  \\ \\_\\    \\ \\_____\\\s
                          \\/_/\\/_/   \\/_/   \\/_____/   \\/_/\\/_/   \\/_/     \\/_____/\s
                        """
        );
    }
}
