package eapli.aisafe.flightmanagement.repositories;

import eapli.aisafe.flightmanagement.application.FlightValidationPreview;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends DomainRepository<FlightDesignator, Flight> {

    default Optional<Flight> findByDesignator(final FlightDesignator designator) {
        return ofIdentity(designator);
    }

    /**
     * {@code true} if a flight exists for this tail number with a schedule and
     * scheduled departure at or after {@code asOf} (still pending / not in the past).
     */
    boolean existsPendingFlightForAircraft(String aircraftRegistration, LocalDateTime asOf);

    /**
     * Flights with a persisted plan JSON and schedule overlapping the interval.
     */
    List<Flight> findScheduledWithFlightPlan(LocalDateTime start, LocalDateTime end);

    /**
     * Lightweight previews of DRAFT flights owned by the given pilot (US085 picker).
     * Implementations must NOT load LOB columns (dslContent / jsonContent).
     */
    List<FlightValidationPreview> findDraftFlightsForPilot(SystemUser pilotSystemUser);

    /**
     * {@code true} if a flight references the given pilot (via {@code Flight.pilot.systemUser}),
     * has a plan in {@code DRAFT}, {@code SUBMITTED_FOR_SIMULATION}, or {@code SIM_APPROVED},
     * and is still pending ({@code schedule.scheduledDeparture >= asOf}).
     */
    boolean existsActiveFlightForPilot(SystemUser pilotSystemUser, LocalDateTime asOf);

    /**
     * {@code true} if a flight on {@code routeName} has {@code schedule.scheduledDeparture}
     * on or after {@code deactivationDate} (inclusive) and counts as planned
     * (no plan, or plan status DRAFT / SUBMITTED_FOR_SIMULATION / SIM_APPROVED).
     */
    boolean existsPlannedFlightOnRouteAfter(RouteName routeName, LocalDate deactivationDate);

    /**
     * Latest {@code scheduledDeparture} date among planned flights on {@code routeName} that are still
     * scheduled on or after today ({@code scheduledDeparture.toLocalDate() >= today}), if any.
     */
    default Optional<LocalDate> findLastPlannedFlightDepartureDateOnRoute(final RouteName routeName) {
        final String normalizedRoute = routeName.toString().trim().toUpperCase();
        final LocalDate today = LocalDate.now();
        LocalDate latest = null;
        for (final Flight flight : findAll()) {
            if (!isPlannedFlightOnRoute(flight, normalizedRoute)) {
                continue;
            }
            final LocalDate departure = flight.schedule().scheduledDeparture().toLocalDate();
            if (departure.isBefore(today)) {
                continue;
            }
            if (latest == null || departure.isAfter(latest)) {
                latest = departure;
            }
        }
        return Optional.ofNullable(latest);
    }

    private static boolean isPlannedFlightOnRoute(final Flight flight, final String normalizedRouteName) {
        if (flight.routeName() == null || flight.schedule() == null) {
            return false;
        }
        if (!normalizedRouteName.equals(flight.routeName().trim().toUpperCase())) {
            return false;
        }
        if (flight.flightPlan() == null) {
            return true;
        }
        final FlightPlanStatus status = flight.flightPlan().status();
        return status == FlightPlanStatus.DRAFT
                || status == FlightPlanStatus.SUBMITTED_FOR_SIMULATION
                || status == FlightPlanStatus.SIM_APPROVED;
    }
}
