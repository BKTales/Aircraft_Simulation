package eapli.aisafe.rcomp.tcpclient.presentation.weather;

@SuppressWarnings("squid:S106")
public final class RemoteWeatherDataPrinter {

    private RemoteWeatherDataPrinter() {}

    public static void printTableHeader() {
        System.out.printf("%-6s %-10s %-19s %-19s %-6s %-6s %-6s %-6s %-6s%n",
                "ID", "Area", "Start", "End", "Temp", "Hum", "Press", "Wind°", "Wind");
        System.out.println("-".repeat(95));
    }

    public static void printRow(final String line) {
        final String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            System.out.println(line);
            return;
        }
        System.out.printf("%-6s %-10s %-19s %-19s %-6s %-6s %-6s %-6s %-6s%n",
                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
    }
}
