package eapli.aisafe.weatherdata.repositories;

import eapli.aisafe.Application;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaWeatherDataRepository
        extends JpaAutoTxRepository<WeatherData, Long, Long>
        implements WeatherDataRepository {

    public JpaWeatherDataRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaWeatherDataRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public List<WeatherData> findByAreaAndInterval(final AreaCode areaCode, final LocalDateTime start,
                                                   final LocalDateTime end) {
        final Map<String, Object> params = new HashMap<>();
        params.put("areaCode", areaCode);
        params.put("start", start);
        params.put("end", end);
        return match(
                "e.airControlArea.areaCode = :areaCode"
                        + " AND e.weatherDate.startDateTime <= :end"
                        + " AND e.weatherDate.endDateTime >= :start",
                params);
    }
}