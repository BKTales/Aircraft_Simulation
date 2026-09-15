package eapli.aisafe.flightmanagement.domain.simulation;

import eapli.aisafe.aircontrolarea.domain.AreaCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FlightSimulationReport {

    private final AreaCode evaluatedIn;
    private final ValidationStatus validationResult;
    private final List<FlightStatus> flightStatuses;
    private final List<ScheduleFlightStatus> scheduleFlightStatuses;
    private final List<SafetyViolationEvent> safetyViolations;

    public FlightSimulationReport(final AreaCode evaluatedIn,
                                    final ValidationStatus validationResult,
                                    final List<FlightStatus> flightStatuses,
                                    final List<ScheduleFlightStatus> scheduleFlightStatuses,
                                    final List<SafetyViolationEvent> safetyViolations) {
        if (evaluatedIn == null) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (validationResult == null) {
            throw new IllegalArgumentException("Validation result is required.");
        }
        this.evaluatedIn = evaluatedIn;
        this.validationResult = validationResult;
        this.flightStatuses = Collections.unmodifiableList(new ArrayList<>(flightStatuses));
        this.scheduleFlightStatuses = Collections.unmodifiableList(new ArrayList<>(scheduleFlightStatuses));
        this.safetyViolations = Collections.unmodifiableList(new ArrayList<>(safetyViolations));
    }

    public AreaCode evaluatedIn() {
        return evaluatedIn;
    }

    public ValidationStatus validationResult() {
        return validationResult;
    }

    public boolean passed() {
        return validationResult == ValidationStatus.PASS;
    }

    public List<FlightStatus> flightStatuses() {
        return flightStatuses;
    }

    public List<ScheduleFlightStatus> scheduleFlightStatuses() {
        return scheduleFlightStatuses;
    }

    public List<SafetyViolationEvent> safetyViolations() {
        return safetyViolations;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FlightSimulationReport that = (FlightSimulationReport) o;
        return Objects.equals(evaluatedIn, that.evaluatedIn)
                && validationResult == that.validationResult
                && Objects.equals(flightStatuses, that.flightStatuses)
                && Objects.equals(scheduleFlightStatuses, that.scheduleFlightStatuses)
                && Objects.equals(safetyViolations, that.safetyViolations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluatedIn, validationResult, flightStatuses, scheduleFlightStatuses, safetyViolations);
    }
}
