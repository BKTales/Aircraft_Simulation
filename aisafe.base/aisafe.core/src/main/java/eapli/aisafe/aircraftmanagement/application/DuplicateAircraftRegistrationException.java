package eapli.aisafe.aircraftmanagement.application;

public class DuplicateAircraftRegistrationException extends RuntimeException {

  public DuplicateAircraftRegistrationException(final String message) {
    super(message);
  }
}
