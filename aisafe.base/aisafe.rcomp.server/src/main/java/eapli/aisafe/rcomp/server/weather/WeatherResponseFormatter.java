package eapli.aisafe.rcomp.server.weather;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.weatherdata.domain.WeatherData;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class WeatherResponseFormatter {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private WeatherResponseFormatter() {}

    public static String formatArea(final AirControlArea area) {
        final StringBuilder coords = new StringBuilder();
        for (final GeographicCoords coord : area.getGeographicBoundary().getGeoCords()) {
            if (!coords.isEmpty()) {
                coords.append(',');
            }
            coords.append(coord.getX()).append(':').append(coord.getY());
        }
        return area.identity().toString() + "|" + coords;
    }

    public static String formatWeather(final WeatherData weather) {
        return String.join("|",
                String.valueOf(weather.identity()),
                weather.getAirControlArea().identity().toString(),
                weather.getWeatherDate().getStartDateTime().format(DATE_TIME),
                weather.getWeatherDate().getEndDateTime().format(DATE_TIME),
                Double.toString(weather.getTemperature().getTemperature()),
                Double.toString(weather.getHumidity().getHumidity()),
                Double.toString(weather.getPressure().getPressure()),
                Integer.toString(weather.getWindData().getDirection().getWindDirection()),
                Double.toString(weather.getWindData().getSpeed().getSpeed()));
    }

    public static String joinLines(final List<String> lines) {
        return String.join("\n", lines);
    }
}
