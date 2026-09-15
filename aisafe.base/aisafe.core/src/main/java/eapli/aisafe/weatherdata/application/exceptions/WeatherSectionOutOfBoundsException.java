package eapli.aisafe.weatherdata.application.exceptions;

public class WeatherSectionOutOfBoundsException extends RuntimeException {
    public WeatherSectionOutOfBoundsException() {
        super("Weather area section is not inside the geographic boundary of its Air Control Area");
    }
}
