package eapli.aisafe.weatherdata.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.weatherdata.application.bulk.UnsupportedWeatherDataFormatException;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReader;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReaderFactory;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkRecord;
import eapli.aisafe.weatherdata.application.exceptions.InvalidHumidityValueException;
import eapli.aisafe.weatherdata.application.exceptions.WeatherSectionOutOfBoundsException;
import eapli.aisafe.weatherdata.domain.*;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.application.ApplicationService;
import eapli.framework.domain.repositories.TransactionalContext;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ApplicationService
public class WeatherDataService {

    private final AirControlAreaRepository areaRepo;
    private final WeatherDataRepository weatherRepo;
    private final WeatherComplianceService complianceService;

    public WeatherDataService(final AirControlAreaRepository areaRepo, final WeatherDataRepository weatherRepo) {
        this.areaRepo = areaRepo;
        this.weatherRepo = weatherRepo;
        this.complianceService = new WeatherComplianceService();
    }

    public RegisterWeatherResult registerWeatherData(final String areaCode, final List<float[]> rawCoords,
                                                       final float temp, final int direction, final float speed,
                                                       final float hum, final float press,
                                                       final LocalDateTime start, final LocalDateTime end,
                                                       final TransactionalContext txCtx) {
        return executeInTransaction(txCtx,
                () -> RegisterWeatherResult.success(doRegisterWeatherData(areaCode, rawCoords, temp, direction, speed, hum, press, start, end)),
                this::mapRegisterException);
    }

    public ConsultWeatherResult consultWeatherDataForDay(final String areaCode, final LocalDateTime day) {
        try {
            if (day == null) {
                return ConsultWeatherResult.failure(ConsultWeatherResult.Outcome.INVALID_INPUT, "Day is required.");
            }
            final LocalDateTime startOfDay = day.toLocalDate().atStartOfDay();
            final LocalDateTime endOfDay = day.toLocalDate().atTime(LocalTime.MAX);
            final List<WeatherData> records = weatherRepo.findByAreaAndInterval(AreaCode.valueOf(areaCode), startOfDay, endOfDay);
            return ConsultWeatherResult.success(records);
        } catch (final IllegalArgumentException ex) {
            return ConsultWeatherResult.failure(ConsultWeatherResult.Outcome.INVALID_INPUT, ex.getMessage());
        } catch (final RuntimeException ex) {
            return ConsultWeatherResult.failure(ConsultWeatherResult.Outcome.ERROR,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        }
    }

    public BulkImportWeatherResult importFromFile(final Path file,
                                                  final WeatherDataBulkReaderFactory readerFactory,
                                                  final TransactionalContext txCtx) {
        final WeatherDataBulkReader reader;
        try {
            reader = readerFactory.forFile(file);
        } catch (final UnsupportedWeatherDataFormatException ex) {
            return BulkImportWeatherResult.failureWithMsg(BulkImportWeatherResult.Outcome.UNSUPPORTED_FORMAT, ex.getMessage());
        }

        final List<WeatherDataBulkRecord> records;
        try {
            records = reader.read(file);
        } catch (final IOException ex) {
            return BulkImportWeatherResult.failureWithMsg(BulkImportWeatherResult.Outcome.IO_ERROR, ex.getMessage());
        }

        return executeInTransaction(txCtx, () -> {
            final List<WeatherData> imported = new ArrayList<>();
            for (final WeatherDataBulkRecord record : records) {
                imported.add(doRegisterWeatherData(
                        record.areaCode(),
                        record.rawCoords(),
                        record.temperature(),
                        record.direction(),
                        record.speed(),
                        record.humidity(),
                        record.pressure(),
                        record.start(),
                        record.end()));
            }
            return BulkImportWeatherResult.success(imported);
        }, this::mapBulkException);
    }

    private WeatherData doRegisterWeatherData(final String areaCode, final List<float[]> rawCoords,
                                              final float temp, final int direction, final float speed,
                                              final float hum, final float press,
                                              final LocalDateTime start, final LocalDateTime end) {
        final AirControlArea area = areaRepo.ofIdentity(AreaCode.valueOf(areaCode))
                .orElseThrow(() -> new IllegalArgumentException("Area not found: " + areaCode));

        final List<GeographicCoords> coords = new ArrayList<>();
        for (final float[] point : rawCoords) {
            coords.add(GeographicCoords.valueOf(point[0], point[1]));
        }
        final GeographicBoundary sectionBoundary = GeographicBoundary.valueOf(coords);

        if (!complianceService.isSectionValidInsideArea(area, sectionBoundary)) {
            throw new WeatherSectionOutOfBoundsException();
        }

        final WeatherDate interval = WeatherDate.valueOf(start, end);
        final Temperature temperature = Temperature.valueOf(temp);
        final Humidity humidity = Humidity.valueOf(hum);
        final Pressure pressure = Pressure.valueOf(press);
        final WindDataDirection winDir = WindDataDirection.valueOf(direction);
        final WindDataSpeed winSpeed = WindDataSpeed.valueOf(speed);
        final WindData windData = WindData.valueOf(winDir, winSpeed);
        final WeatherSection weatherSection = WeatherSection.valueOf(sectionBoundary);

        final WeatherData newWeather = new WeatherData(
                interval,
                humidity,
                pressure,
                temperature,
                windData,
                weatherSection,
                area
        );

        return weatherRepo.save(newWeather);
    }

    private <T> T executeInTransaction(final TransactionalContext txCtx,
                                         final Supplier<T> action,
                                         final Function<Throwable, T> mapper) {
        if (txCtx != null) {
            txCtx.beginTransaction();
        }

        try {
            final T result = action.get();
            if (txCtx != null) {
                txCtx.commit();
                txCtx.close();
            }
            return result;
        } catch (final Throwable t) {
            if (txCtx != null) {
                try {
                    txCtx.rollback();
                    txCtx.close();
                } catch (final Exception ignored) {
                    // rollback/close best-effort
                }
            }
            return mapper.apply(t);
        }
    }

    private RegisterWeatherResult mapRegisterException(final Throwable t) {
        if (t instanceof WeatherSectionOutOfBoundsException) {
            return RegisterWeatherResult.failure(RegisterWeatherResult.Outcome.SECTION_OUT_OF_BOUNDS);
        }
        if (t instanceof InvalidHumidityValueException) {
            return RegisterWeatherResult.failure(RegisterWeatherResult.Outcome.INVALID_HUMIDITY);
        }
        if (t instanceof IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            if (msg != null && msg.startsWith("Area not found:")) {
                return RegisterWeatherResult.failure(RegisterWeatherResult.Outcome.AREA_NOT_FOUND);
            }
            return RegisterWeatherResult.failureWithMsg(RegisterWeatherResult.Outcome.INVALID_INPUT, msg);
        }
        if (t instanceof DateTimeParseException) {
            return RegisterWeatherResult.failure(RegisterWeatherResult.Outcome.INVALID_INPUT);
        }
        return RegisterWeatherResult.failureWithMsg(RegisterWeatherResult.Outcome.ERROR,
                t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
    }

    private BulkImportWeatherResult mapBulkException(final Throwable t) {
        final RegisterWeatherResult mapped = mapRegisterException(t);
        return switch (mapped.outcome()) {
            case AREA_NOT_FOUND -> BulkImportWeatherResult.failure(BulkImportWeatherResult.Outcome.AREA_NOT_FOUND);
            case SECTION_OUT_OF_BOUNDS -> BulkImportWeatherResult.failure(BulkImportWeatherResult.Outcome.SECTION_OUT_OF_BOUNDS);
            case INVALID_HUMIDITY -> BulkImportWeatherResult.failure(BulkImportWeatherResult.Outcome.INVALID_HUMIDITY);
            case INVALID_INPUT -> BulkImportWeatherResult.failureWithMsg(
                    BulkImportWeatherResult.Outcome.INVALID_INPUT, mapped.message());
            case ERROR -> BulkImportWeatherResult.failureWithMsg(
                    BulkImportWeatherResult.Outcome.ERROR, mapped.message());
            case SUCCESS -> BulkImportWeatherResult.failure(BulkImportWeatherResult.Outcome.ERROR);
        };
    }
}
