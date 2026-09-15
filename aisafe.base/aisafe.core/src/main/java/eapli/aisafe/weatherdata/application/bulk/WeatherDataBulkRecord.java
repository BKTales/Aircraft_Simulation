package eapli.aisafe.weatherdata.application.bulk;

import java.time.LocalDateTime;
import java.util.List;

public record WeatherDataBulkRecord(
        String areaCode,
        List<float[]> rawCoords,
        float temperature,
        int direction,
        float speed,
        float humidity,
        float pressure,
        LocalDateTime start,
        LocalDateTime end
) {
}
