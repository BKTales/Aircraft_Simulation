package eapli.aisafe.app.common.console;

/**
 * AISafe ASCII banner shown before login (clears screen first).
 */
@SuppressWarnings("squid:S106")
public final class AisafeConsoleBanner {

    private AisafeConsoleBanner() {
    }

    public static void printLogo() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
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
