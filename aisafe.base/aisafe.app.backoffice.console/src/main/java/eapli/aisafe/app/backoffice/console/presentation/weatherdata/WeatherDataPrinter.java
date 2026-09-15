package eapli.aisafe.app.backoffice.console.presentation.weatherdata;

import eapli.aisafe.weatherdata.domain.WeatherData;

import java.time.format.DateTimeFormatter;

@SuppressWarnings("squid:S106")
public final class WeatherDataPrinter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private WeatherDataPrinter() {
        // utility
    }

    public static void printTableHeader() {
        System.out.printf("%-6s %-10s %-19s %-19s %-6s %-6s %-6s %-6s %-6s%n",
                "ID", "Area", "Start", "End", "Temp", "Hum", "Press", "Wind°", "Wind");
        System.out.println("-".repeat(95));
    }

    public static void printRow(final WeatherData weather) {
        System.out.printf("%-6s %-10s %-19s %-19s %-6.1f %-6.1f %-6.1f %-6d %-6.1f%n",
                weather.identity(),
                weather.getAirControlArea().identity(),
                weather.getWeatherDate().getStartDateTime().format(DATE_TIME_FORMAT),
                weather.getWeatherDate().getEndDateTime().format(DATE_TIME_FORMAT),
                weather.getTemperature().getTemperature(),
                weather.getHumidity().getHumidity(),
                weather.getPressure().getPressure(),
                weather.getWindData().getDirection().getWindDirection(),
                weather.getWindData().getSpeed().getSpeed());
    }
}
