package eapli.aisafe.weatherdata.application;

import eapli.aisafe.weatherdata.domain.WeatherData;

public final class RegisterWeatherResult {

    public enum Outcome {
        SUCCESS,
        AREA_NOT_FOUND,
        SECTION_OUT_OF_BOUNDS,
        INVALID_INPUT,
        INVALID_HUMIDITY,
        ERROR
    }

    private final Outcome outcome;
    private final WeatherData weatherData;
    private final String message;

    private RegisterWeatherResult(final Outcome outcome, final WeatherData weatherData, final String message) {
        this.outcome = outcome;
        this.weatherData = weatherData;
        this.message = message;
    }

    public static RegisterWeatherResult success(final WeatherData weatherData) {
        return new RegisterWeatherResult(Outcome.SUCCESS, weatherData, null);
    }

    public static RegisterWeatherResult failure(final Outcome outcome) {
        return new RegisterWeatherResult(outcome, null, null);
    }

    public static RegisterWeatherResult failureWithMsg(final Outcome outcome, final String message) {
        return new RegisterWeatherResult(outcome, null, message);
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public Outcome outcome() {
        return outcome;
    }

    public WeatherData weatherData() {
        return weatherData;
    }

    public String message() {
        return message;
    }
}
