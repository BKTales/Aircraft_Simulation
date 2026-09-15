package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;

import java.util.Optional;

public final class CreateFlightPlanResult {

    public enum Outcome {
        SUCCESS, FAILURE, NEEDS_CONFIRMATION
    }

    private final Outcome outcome;
    private final FlightDesignator designator;
    private final String errorMessage;
    private final FlightPlanStatus existingPlanStatus;
    private final boolean replaced;

    private CreateFlightPlanResult(final Outcome outcome,
                                   final FlightDesignator designator,
                                   final String errorMessage,
                                   final FlightPlanStatus existingPlanStatus,
                                   final boolean replaced) {
        this.outcome = outcome;
        this.designator = designator;
        this.errorMessage = errorMessage;
        this.existingPlanStatus = existingPlanStatus;
        this.replaced = replaced;
    }

    public static CreateFlightPlanResult success(final FlightDesignator designator) {
        return new CreateFlightPlanResult(Outcome.SUCCESS, designator, null, null, false);
    }

    public static CreateFlightPlanResult replaced(final FlightDesignator designator,
                                                  final FlightPlanStatus replacedFromStatus) {
        return new CreateFlightPlanResult(Outcome.SUCCESS, designator, null, replacedFromStatus, true);
    }

    public static CreateFlightPlanResult failure(final String message) {
        return new CreateFlightPlanResult(Outcome.FAILURE, null, message, null, false);
    }

    public static CreateFlightPlanResult needsConfirmation(final FlightDesignator designator,
                                                             final FlightPlanStatus currentStatus) {
        final String message = String.format(
                "Flight %s already has a flight plan. Status: %s. Do you want to replace it? (y/n)",
                designator, currentStatus);
        return new CreateFlightPlanResult(Outcome.NEEDS_CONFIRMATION, designator, message, currentStatus, false);
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public boolean needsConfirmation() {
        return outcome == Outcome.NEEDS_CONFIRMATION;
    }

    public boolean replaced() {
        return replaced;
    }

    public Optional<FlightDesignator> designator() {
        return Optional.ofNullable(designator);
    }

    public Optional<FlightPlanStatus> existingPlanStatus() {
        return Optional.ofNullable(existingPlanStatus);
    }

    public Optional<FlightPlanStatus> replacedFromStatus() {
        return replaced ? Optional.ofNullable(existingPlanStatus) : Optional.empty();
    }

    public String errorMessage() {
        return errorMessage;
    }
}
