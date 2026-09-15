package eapli.aisafe.flightmanagement.application.exceptions;

import java.time.YearMonth;

public class NoMonthlySimulationDataException extends RuntimeException {

    public NoMonthlySimulationDataException(final String areaCode, final YearMonth period) {
        super("No simulation summaries found for area " + areaCode + " in " + period + ".");
    }
}
