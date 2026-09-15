package eapli.aisafe.weatherdata.application;

import eapli.aisafe.weatherdata.domain.WeatherData;

import java.util.Collections;
import java.util.List;

public final class BulkImportWeatherResult {

    public enum Outcome {
        SUCCESS,
        UNSUPPORTED_FORMAT,
        IO_ERROR,
        AREA_NOT_FOUND,
        SECTION_OUT_OF_BOUNDS,
        INVALID_INPUT,
        INVALID_HUMIDITY,
        ERROR
    }

    private final Outcome outcome;
    private final List<WeatherData> imported;
    private final String message;

    private BulkImportWeatherResult(final Outcome outcome, final List<WeatherData> imported, final String message) {
        this.outcome = outcome;
        this.imported = imported != null ? imported : Collections.emptyList();
        this.message = message;
    }

    public static BulkImportWeatherResult success(final List<WeatherData> imported) {
        return new BulkImportWeatherResult(Outcome.SUCCESS, imported, null);
    }

    public static BulkImportWeatherResult failure(final Outcome outcome) {
        return new BulkImportWeatherResult(outcome, null, null);
    }

    public static BulkImportWeatherResult failureWithMsg(final Outcome outcome, final String message) {
        return new BulkImportWeatherResult(outcome, null, message);
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public List<WeatherData> imported() {
        return imported;
    }

    public String message() {
        return message;
    }
}
