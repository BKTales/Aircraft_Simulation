package eapli.aisafe.weatherdata.application;

import eapli.aisafe.weatherdata.domain.WeatherData;

import java.util.Collections;
import java.util.List;

public final class ConsultWeatherResult {

    public enum Outcome {
        SUCCESS,
        INVALID_INPUT,
        ERROR
    }

    private final Outcome outcome;
    private final List<WeatherData> records;
    private final String message;

    private ConsultWeatherResult(final Outcome outcome, final List<WeatherData> records, final String message) {
        this.outcome = outcome;
        this.records = records != null ? records : Collections.emptyList();
        this.message = message;
    }

    public static ConsultWeatherResult success(final List<WeatherData> records) {
        return new ConsultWeatherResult(Outcome.SUCCESS, records, null);
    }

    public static ConsultWeatherResult failure(final Outcome outcome, final String message) {
        return new ConsultWeatherResult(outcome, null, message);
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public List<WeatherData> records() {
        return records;
    }

    public String message() {
        return message;
    }
}
