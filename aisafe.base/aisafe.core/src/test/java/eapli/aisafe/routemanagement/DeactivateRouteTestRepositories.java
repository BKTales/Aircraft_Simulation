package eapli.aisafe.routemanagement;

import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Per-test repositories: framework {@code InMemoryDomainRepository} uses JVM-wide static storage.
 */
public final class DeactivateRouteTestRepositories {

    private DeactivateRouteTestRepositories() {
    }

    public static RouteRepository newRouteRepository() {
        return new IsolatedRouteRepository();
    }

    public static FlightRepository newFlightRepository() {
        return new IsolatedFlightRepository();
    }

    private static final class IsolatedRouteRepository implements RouteRepository {
        private final Map<RouteName, Route> byId = new HashMap<>();

        @Override
        public Route save(final Route entity) {
            byId.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<Route> ofIdentity(final RouteName id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Iterable<Route> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public boolean containsOfIdentity(final RouteName id) {
            return byId.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final RouteName entityId) {
            byId.remove(entityId);
        }

        @Override
        public void delete(final Route entity) {
            deleteOfIdentity(entity.identity());
        }

        @Override
        public long count() {
            return byId.size();
        }
    }

    static final class IsolatedFlightRepository implements FlightRepository {
        private final Map<FlightDesignator, Flight> byId = new HashMap<>();

        @Override
        public boolean existsPendingFlightForAircraft(final String aircraftRegistration, final LocalDateTime asOf) {
            return false;
        }

        @Override
        public boolean existsActiveFlightForPilot(final SystemUser pilotSystemUser, final LocalDateTime asOf) {
            return false;
        }

        @Override
        public List<Flight> findScheduledWithFlightPlan(final LocalDateTime start, final LocalDateTime end) {
            return List.of();
        }

        @Override
        public java.util.List<eapli.aisafe.flightmanagement.application.FlightValidationPreview> findDraftFlightsForPilot(final SystemUser pilotSystemUser) {
            return java.util.List.of();
        }

        @Override
        public boolean existsPlannedFlightOnRouteAfter(final RouteName routeName, final LocalDate deactivationDate) {
            final String normalizedRoute = routeName.toString().trim().toUpperCase();
            for (final Flight f : findAll()) {
                if (matchesPlannedOnRoute(normalizedRoute, f, deactivationDate)) {
                    return true;
                }
            }
            return false;
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

        @Override
        public Flight save(final Flight entity) {
            byId.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<Flight> ofIdentity(final FlightDesignator id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Iterable<Flight> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public boolean containsOfIdentity(final FlightDesignator id) {
            return byId.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final FlightDesignator entityId) {
            byId.remove(entityId);
        }

        @Override
        public void delete(final Flight entity) {
            deleteOfIdentity(entity.identity());
        }

        @Override
        public long count() {
            return byId.size();
        }
    }
}
