package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.flightmanagement.application.FlightValidationPreview;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryFlightRepository extends InMemoryDomainRepository<Flight, FlightDesignator>
        implements FlightRepository {

    @Override
    public boolean existsPendingFlightForAircraft(final String aircraftRegistration, final LocalDateTime asOf) {
        final String reg = new AircraftRegistration(aircraftRegistration).toString();
        return matchOne(f -> matchesPending(reg, f, asOf)).isPresent();
    }

    @Override
    public boolean existsActiveFlightForPilot(final SystemUser pilotSystemUser, final LocalDateTime asOf) {
        return matchOne(f -> matchesBlockingPlanForPilot(pilotSystemUser, f, asOf)).isPresent();
    }

    private static boolean matchesBlockingPlanForPilot(
            final SystemUser pilotSystemUser, final Flight f, final LocalDateTime asOf) {
        if (f.pilot() == null || f.flightPlan() == null || f.schedule() == null || asOf == null) {
            return false;
        }
        if (!f.pilot().systemUser().equals(pilotSystemUser)) {
            return false;
        }
        if (f.schedule().scheduledDeparture().isBefore(asOf)) {
            return false;
        }
        final FlightPlanStatus status = f.flightPlan().status();
        return status == FlightPlanStatus.DRAFT
                || status == FlightPlanStatus.SUBMITTED_FOR_SIMULATION
                || status == FlightPlanStatus.SIM_APPROVED;
    }

    @Override
    public boolean existsPlannedFlightOnRouteAfter(final RouteName routeName, final LocalDate deactivationDate) {
        final String normalizedRoute = routeName.toString().trim().toUpperCase();
        return matchOne(f -> matchesPlannedOnRoute(normalizedRoute, f, deactivationDate)).isPresent();
    }

    @Override
    public List<Flight> findScheduledWithFlightPlan(final LocalDateTime start, final LocalDateTime end) {
        final List<Flight> result = new ArrayList<>();
        for (final Flight flight : findAll()) {
            if (flight.flightPlan() == null || flight.schedule() == null) {
                continue;
            }
            if (flight.flightPlan().jsonContent() == null || flight.flightPlan().jsonContent().isBlank()) {
                continue;
            }
            if (flight.schedule().scheduledDeparture().isAfter(end)
                    || flight.schedule().scheduledArrival().isBefore(start)) {
                continue;
            }
            result.add(flight);
        }
        return result;
    }

    @Override
    public List<FlightValidationPreview> findDraftFlightsForPilot(final SystemUser pilotSystemUser) {
        final List<FlightValidationPreview> result = new ArrayList<>();
        for (final Flight flight : findAll()) {
            if (flight.pilot() == null || flight.flightPlan() == null) {
                continue;
            }
            if (!flight.pilot().systemUser().equals(pilotSystemUser)) {
                continue;
            }
            if (flight.flightPlan().status() != FlightPlanStatus.DRAFT) {
                continue;
            }
            result.add(new FlightValidationPreview(
                    flight.identity().toString(),
                    flight.routeName(),
                    null, null,
                    flight.aircraftRegistration(),
                    flight.flightPlan().status(),
                    flight.schedule() != null ? flight.schedule().scheduledDeparture() : null,
                    null));
        }
        return result;
    }

    private static boolean matchesPlannedOnRoute(
            final String normalizedRouteName, final Flight f, final LocalDate deactivationDate) {
        if (f.routeName() == null || f.schedule() == null || deactivationDate == null) {
            return false;
        }
        if (!normalizedRouteName.equals(f.routeName().trim().toUpperCase())) {
            return false;
        }
        if (f.schedule().scheduledDeparture().toLocalDate().isBefore(deactivationDate)) {
            return false;
        }
        if (f.flightPlan() == null) {
            return true;
        }
        final FlightPlanStatus status = f.flightPlan().status();
        return status == FlightPlanStatus.DRAFT
                || status == FlightPlanStatus.SUBMITTED_FOR_SIMULATION
                || status == FlightPlanStatus.SIM_APPROVED;
    }

    private static boolean matchesPending(final String normalizedReg, final Flight f, final LocalDateTime asOf) {
        if (f.aircraftRegistration() == null || f.schedule() == null) {
            return false;
        }
        final String fr;
        try {
            fr = new AircraftRegistration(f.aircraftRegistration()).toString();
        } catch (final RuntimeException ex) {
            return false;
        }
        return normalizedReg.equals(fr) && !f.schedule().scheduledDeparture().isBefore(asOf);
    }
}
