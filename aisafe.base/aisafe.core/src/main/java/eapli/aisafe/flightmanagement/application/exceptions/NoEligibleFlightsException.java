package eapli.aisafe.flightmanagement.application.exceptions;

import java.time.LocalDateTime;

public class NoEligibleFlightsException extends RuntimeException {

    public NoEligibleFlightsException(final String areaCode,
                                      final LocalDateTime start,
                                      final LocalDateTime end) {
        super("No eligible flights for area " + areaCode + " between " + start + " and " + end + ".");
    }
}
