package eapli.aisafe.weatherdata.application.bulk;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface WeatherDataBulkReader {

    List<WeatherDataBulkRecord> read(Path file) throws IOException;

    boolean supports(Path file);
}
