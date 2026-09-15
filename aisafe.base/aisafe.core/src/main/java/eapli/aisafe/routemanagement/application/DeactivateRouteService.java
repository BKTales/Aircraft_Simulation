package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.DeactivationDate;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DeactivateRouteService {

    public static final String PLANNED_FLIGHT_BLOCKS_DEACTIVATION_MESSAGE =
            "There is a flight planned for that date or a future date in that route.";

    private final RouteRepository routes;
    private final FlightRepository flights;

    public DeactivateRouteService(final RouteRepository routes, final FlightRepository flights) {
        if (routes == null || flights == null) {
            throw new IllegalArgumentException("Route and flight repositories are required.");
        }
        this.routes = routes;
        this.flights = flights;
    }

    public List<Route> listActiveRoutesByCompany(final AirTransportCompany company) {
        return listActiveRouteOptionsByCompany(company).stream()
                .map(ActiveRouteOption::route)
                .toList();
    }

    public List<ActiveRouteOption> listActiveRouteOptionsByCompany(final AirTransportCompany company) {
        Objects.requireNonNull(company, "Company is required.");
        final List<ActiveRouteOption> active = new ArrayList<>();
        for (final Route route : routes.findByCompany(company)) {
            if (!route.isActive()) {
                continue;
            }
            final Optional<LocalDate> lastPlanned =
                    flights.findLastPlannedFlightDepartureDateOnRoute(route.identity());
            active.add(new ActiveRouteOption(route, lastPlanned));
        }
        active.sort(Comparator.comparing(o -> o.route().identity().toString()));
        return active;
    }

    public Route deactivateRoute(final String routeName,
                                 final LocalDate deactivationDate,
                                 final AirTransportCompany company) {
        Objects.requireNonNull(company, "Company is required.");
        if (deactivationDate == null) {
            throw new IllegalArgumentException("Deactivation date is required.");
        }
        if (deactivationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deactivation date cannot be in the past.");
        }

        final RouteName normalizedName = RouteName.valueOf(routeName);
        final Route route = routes.ofIdentity(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeName));

        if (!route.companyIATACode().equals(company.identity())) {
            throw new IllegalArgumentException("Route does not belong to the current company.");
        }
        if (!route.isActive()) {
            throw new RouteAlreadyDeactivatedException("Route is already deactivated.");
        }
        if (flights.existsPlannedFlightOnRouteAfter(normalizedName, deactivationDate)) {
            throw new RouteHasPlannedFlightsException(PLANNED_FLIGHT_BLOCKS_DEACTIVATION_MESSAGE);
        }

        route.deactivate(DeactivationDate.valueOf(deactivationDate));
        return routes.save(route);
    }
}
