package eapli.aisafe.weatherdata.application.bulk.csv;

import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReader;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvWeatherDataBulkReader implements WeatherDataBulkReader {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm", Locale.US);

    @Override
    public boolean supports(final Path file) {
        final String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".csv");
    }

    @Override
    public List<WeatherDataBulkRecord> read(final Path file) throws IOException {
        final List<String> lines = Files.readAllLines(file);
        final List<WeatherDataBulkRecord> records = new ArrayList<>();
        for (final String rawLine : lines) {
            if (rawLine == null || rawLine.isBlank()) {
                continue;
            }
            if (rawLine.startsWith("ACA,")) {
                continue;
            }
            final String[] cols = rawLine.split(",");
            if (cols.length < 12) {
                continue;
            }
            records.add(parseRecord(cols));
        }
        return records;
    }

    private static WeatherDataBulkRecord parseRecord(final String[] cols) {
        final String areaCode = cols[0].trim();
        final float lat1 = Float.parseFloat(cols[1].trim());
        final float lon1 = Float.parseFloat(cols[2].trim());
        final float lat2 = Float.parseFloat(cols[3].trim());
        final float lon2 = Float.parseFloat(cols[4].trim());
        final int direction = Math.round(Float.parseFloat(cols[7].trim()));
        final float speed = Float.parseFloat(cols[8].trim());
        final LocalDate date = LocalDate.parse(cols[9].trim(), DATE_FORMATTER);
        final LocalTime startTime = LocalTime.parse(cols[10].trim(), TIME_FORMATTER);
        final LocalTime endTime = LocalTime.parse(cols[11].trim(), TIME_FORMATTER);
        return new WeatherDataBulkRecord(
                areaCode,
                rectangleToPolygon(lat1, lon1, lat2, lon2),
                20.0f,
                direction,
                speed,
                50.0f,
                1013.0f,
                LocalDateTime.of(date, startTime),
                LocalDateTime.of(date, endTime)
        );
    }

    private static List<float[]> rectangleToPolygon(final float lat1, final float lon1,
                                                    final float lat2, final float lon2) {
        final List<float[]> polygon = new ArrayList<>();
        polygon.add(new float[] {lat1, lon1});
        polygon.add(new float[] {lat1, lon2});
        polygon.add(new float[] {lat2, lon2});
        polygon.add(new float[] {lat2, lon1});
        return polygon;
    }
}
