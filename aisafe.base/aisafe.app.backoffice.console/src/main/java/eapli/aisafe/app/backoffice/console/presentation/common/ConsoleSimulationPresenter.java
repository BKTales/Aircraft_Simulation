package eapli.aisafe.app.backoffice.console.presentation.common;

/**
 * Shared console presentation for flight-plan validation and area simulation
 * (US085 / US100): ANSI colours, a step header, pass/fail lines and a
 * non-blocking "Running simulation ..." spinner.
 */
@SuppressWarnings("squid:S106")
public final class ConsoleSimulationPresenter {

    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String BOLD = "\033[1m";
    private static final String RESET = "\033[0m";

    private static final String[] SPINNER_FRAMES = {".", "..", "..."};
    private static final int SPINNER_INTERVAL_MS = 350;
    private static final int CLEAR_WIDTH = 50;

    private ConsoleSimulationPresenter() {}

    public static void step(final int number, final int total, final String label) {
        System.out.println();
        System.out.println(BOLD + "Step " + number + "/" + total + " - " + label + RESET);
    }

    public static void info(final String message) {
        System.out.println("  " + message);
    }

    public static void ok(final String message) {
        System.out.println(GREEN + "  " + message + RESET);
    }

    public static void warn(final String message) {
        System.out.println(YELLOW + "  " + message + RESET);
    }

    public static void pass(final String message) {
        System.out.println(GREEN + message + RESET);
    }

    public static void fail(final String message) {
        System.out.println(RED + message + RESET);
    }

    /** Starts a daemon spinner that clears its line and prints a newline when stopped. */
    public static Spinner startSpinner(final String label) {
        final Spinner spinner = new Spinner(label);
        spinner.start();
        return spinner;
    }

    public static final class Spinner {

        private final Thread thread;

        private Spinner(final String label) {
            this.thread = new Thread(() -> run(label), "simulation-spinner");
            this.thread.setDaemon(true);
        }

        private static void run(final String label) {
            int frame = 0;
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("\r  " + label + " " + SPINNER_FRAMES[frame % SPINNER_FRAMES.length] + "   ");
                System.out.flush();
                frame++;
                try {
                    Thread.sleep(SPINNER_INTERVAL_MS);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.print("\r" + " ".repeat(CLEAR_WIDTH) + "\r");
            System.out.flush();
        }

        private void start() {
            thread.start();
        }

        public void stop() {
            thread.interrupt();
            try {
                thread.join(2_000);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
