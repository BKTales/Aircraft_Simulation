package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.aisafe.flightmanagement.application.InsertWeatherInFlightController;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.Optional;

@SuppressWarnings("squid:S106")
public class AttachWeatherToFlightUI extends AbstractUI {

    private final InsertWeatherInFlightController controller = new InsertWeatherInFlightController();

    @Override
    protected boolean doShow() {
        final String flightDesignator = Console.readLine("Flight designator (e.g., TP1234A):");
        final long weatherDataId = Console.readLong("Weather Data ID:");

        final Optional<WeatherData> weatherOpt = controller.weatherDataById(weatherDataId);
        if (weatherOpt.isEmpty()) {
            System.out.println("\nError: Weather data not found with ID " + weatherDataId + ".");
            return false;
        }

        final WeatherData weather = weatherOpt.get();
        System.out.println("\n--- Selected Weather Data ---");
        System.out.println("  ID:    " + weather.identity());
        System.out.println("  Area:  " + weather.getAirControlArea().identity());
        System.out.println("  From:  " + weather.getWeatherDate().getStartDateTime());
        System.out.println("  To:    " + weather.getWeatherDate().getEndDateTime());
        System.out.println("  Temp:  " + weather.getTemperature().getTemperature() + " ºC");

        final String confirm = Console.readLine(
                "\nAttach this weather data to flight " + flightDesignator + "? (y/n):");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final Flight flight = controller.attachWeatherToFlight(flightDesignator, weatherDataId);
            System.out.printf("%nWeather data %d attached to flight %s.%n",
                    weatherDataId, flight.identity());
        } catch (final Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Attach Weather Data to Flight (US082)";
    }
}
