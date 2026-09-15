package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.application.bulk.UnsupportedWeatherDataFormatException;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReaderFactory;
import eapli.aisafe.weatherdata.application.bulk.csv.CsvWeatherDataBulkReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherDataBulkReaderFactoryTest {

    @Test
    void forFileReturnsCsvReaderForCsvExtension() {
        final WeatherDataBulkReaderFactory factory = new WeatherDataBulkReaderFactory();

        assertInstanceOf(CsvWeatherDataBulkReader.class, factory.forFile(Path.of("weather.csv")));
    }

    @Test
    void forFileRejectsUnsupportedExtensions() {
        final WeatherDataBulkReaderFactory factory = new WeatherDataBulkReaderFactory();

        assertThrows(UnsupportedWeatherDataFormatException.class, () -> factory.forFile(Path.of("weather.xml")));
        assertThrows(UnsupportedWeatherDataFormatException.class, () -> factory.forFile(Path.of("weather.txt")));
    }

    @Test
    void rejectsEmptyReaderCollection() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherDataBulkReaderFactory(java.util.List.of()));
    }
}
