package eapli.aisafe.weatherdata.application.exceptions;

public class InvalidHumidityValueException extends RuntimeException {
    public InvalidHumidityValueException() {
        super("Invalid percentage value for humidity.");
    }
}
