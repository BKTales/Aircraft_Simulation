package eapli.aisafe.weatherdata.application.bulk;

import java.nio.file.Path;

public class UnsupportedWeatherDataFormatException extends RuntimeException {

    public UnsupportedWeatherDataFormatException(final Path file) {
        super("Unsupported weather data file format: " + file.getFileName());
    }
}
