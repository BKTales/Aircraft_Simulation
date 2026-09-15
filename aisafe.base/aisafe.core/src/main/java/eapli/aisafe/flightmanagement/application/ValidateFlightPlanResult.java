package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;

import java.util.List;

public record ValidateFlightPlanResult(
        boolean passed,
        String flightDesignator,
        FlightPlanStatus status,
        String message,
        List<String> errors,
        List<String> simulationLog,
        String dslContent) {

    public ValidateFlightPlanResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
        simulationLog = simulationLog == null ? List.of() : List.copyOf(simulationLog);
    }

    public boolean dslFailure() {
        return !errors.isEmpty() && status == FlightPlanStatus.DRAFT;
    }

    public static ValidateFlightPlanResult dslFailure(final String designator,
                                                      final List<String> errors,
                                                      final String dslContent) {
        return new ValidateFlightPlanResult(
                false,
                designator,
                FlightPlanStatus.DRAFT,
                "DSL validation failed.",
                errors,
                List.of(),
                dslContent);
    }

    public static ValidateFlightPlanResult approved(final String designator, final List<String> simulationLog) {
        return new ValidateFlightPlanResult(
                true,
                designator,
                FlightPlanStatus.SIM_APPROVED,
                "Flight plan approved.",
                List.of(),
                simulationLog,
                null);
    }

    public static ValidateFlightPlanResult blocked(final String designator, final String message) {
        return new ValidateFlightPlanResult(
                false,
                designator,
                FlightPlanStatus.DRAFT,
                message,
                List.of(),
                List.of(),
                null);
    }

    public static ValidateFlightPlanResult rejected(final String designator,
                                                    final String message,
                                                    final List<String> simulationLog) {
        return new ValidateFlightPlanResult(
                false,
                designator,
                FlightPlanStatus.SIM_REJECTED,
                message,
                List.of(),
                simulationLog,
                null);
    }
}
