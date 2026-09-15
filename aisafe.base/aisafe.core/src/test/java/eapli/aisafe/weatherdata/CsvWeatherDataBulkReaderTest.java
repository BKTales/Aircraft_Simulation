package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkRecord;
import eapli.aisafe.weatherdata.application.bulk.csv.CsvWeatherDataBulkReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWeatherDataBulkReaderTest {

    @Test
    void parsesDataRowsAndSkipsHeaders() throws IOException {
        final Path tmp = Files.createTempFile("weather-reader-", ".csv");
        Files.writeString(tmp, String.join(System.lineSeparator(),
                "ACA,Lat.,Long.,Lat.,Long.,Alt_inf (ft),Alt_Sup (ft),Direction (degree),Value (knot),Day,Star,End",
                "121,43.840454,-9.795711,40.225,-7.9501,0,1000,90,28.75,6/22/2026,5:00,8:15"));
        final CsvWeatherDataBulkReader reader = new CsvWeatherDataBulkReader();

        final List<WeatherDataBulkRecord> rows = reader.read(tmp);

        assertEquals(1, rows.size());
        assertEquals("121", rows.get(0).areaCode());
        assertEquals(4, rows.get(0).rawCoords().size());
        assertEquals(90, rows.get(0).direction());
    }

    @Test
    void supportsCsvExtensionOnly() {
        final CsvWeatherDataBulkReader reader = new CsvWeatherDataBulkReader();

        assertTrue(reader.supports(Path.of("data.CSV")));
        assertTrue(reader.supports(Path.of("folder/weather.csv")));
    }
}
