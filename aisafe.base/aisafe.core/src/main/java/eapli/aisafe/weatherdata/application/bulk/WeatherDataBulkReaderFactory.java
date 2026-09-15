package eapli.aisafe.weatherdata.application.bulk;

import eapli.aisafe.weatherdata.application.bulk.csv.CsvWeatherDataBulkReader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class WeatherDataBulkReaderFactory {

    private final List<WeatherDataBulkReader> readers;

    public WeatherDataBulkReaderFactory() {
        this(List.of(new CsvWeatherDataBulkReader()));
    }

    public WeatherDataBulkReaderFactory(final Collection<WeatherDataBulkReader> readers) {
        if (readers == null || readers.isEmpty()) {
            throw new IllegalArgumentException("At least one bulk reader is required.");
        }
        this.readers = List.copyOf(readers);
    }

    public WeatherDataBulkReader forFile(final Path file) {
        for (final WeatherDataBulkReader reader : readers) {
            if (reader.supports(file)) {
                return reader;
            }
        }
        throw new UnsupportedWeatherDataFormatException(file);
    }
}
