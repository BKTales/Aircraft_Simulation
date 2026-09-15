package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.routemanagement.domain.RecurringScheduleEntry;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.domain.RouteRecurringSchedule;
import eapli.aisafe.routemanagement.domain.RouteSchedule;
import eapli.aisafe.routemanagement.repositories.RouteRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class RouteService {

    private final RouteRepository routes;
    private final AirportRepository airports;

    public RouteService(final RouteRepository routes, final AirportRepository airports) {
        if (routes == null || airports == null) {
            throw new IllegalArgumentException("Route and airport repositories are required.");
        }
        this.routes = routes;
        this.airports = airports;
    }

    public String validateRouteName(final String routeName) {
        final RouteName normalizedRouteName = RouteName.valueOf(routeName);
        if (routes.existsByName(normalizedRouteName)) {
            throw new RouteAlreadyExistsException("Route name already exists.");
        }
        return normalizedRouteName.toString();
    }

    public Route createCharterRoute(final String routeName,
                                    final String originAirportIata,
                                    final String destinationAirportIata,
                                    final AirTransportCompany airTransportCompany,
                                    final LocalDate scheduledDeparture,
                                    final LocalDate scheduledArrival) {
        Objects.requireNonNull(airTransportCompany, "Air transport company is required.");

        final RouteName normalizedRouteName = RouteName.valueOf(routeName);
        assertRouteNameBelongsToCompany(normalizedRouteName, airTransportCompany);
        assertUniqueRouteName(normalizedRouteName);
        final Airport origin = requireAirport(originAirportIata);
        final Airport destination = requireAirport(destinationAirportIata);

        final RouteSchedule routeSchedule = new RouteSchedule(scheduledDeparture, scheduledArrival);
        final Route route = Route.charterRoute(
                normalizedRouteName, airTransportCompany, origin, destination, routeSchedule);
        return routes.save(route);
    }

    public Route createRegularRoute(final String routeName,
                                    final String originAirportIata,
                                    final String destinationAirportIata,
                                    final AirTransportCompany airTransportCompany,
                                    final List<DayOfWeek> recurringDays) {
        Objects.requireNonNull(airTransportCompany, "Air transport company is required.");
        if (recurringDays == null || recurringDays.isEmpty()) {
            throw new IllegalArgumentException("At least one recurring day is required for regular routes.");
        }

        final RouteName normalizedRouteName = RouteName.valueOf(routeName);
        assertRouteNameBelongsToCompany(normalizedRouteName, airTransportCompany);
        assertUniqueRouteName(normalizedRouteName);
        final Airport origin = requireAirport(originAirportIata);
        final Airport destination = requireAirport(destinationAirportIata);

        final List<RecurringScheduleEntry> entries = recurringDays.stream()
                .map(RecurringScheduleEntry::of)
                .toList();
        final RouteRecurringSchedule routeRecurringSchedule = new RouteRecurringSchedule(entries);
        final Route route = Route.regularRoute(
                normalizedRouteName, airTransportCompany, origin, destination, routeRecurringSchedule);
        return routes.save(route);
    }

    public Iterable<Airport> listAirports() {
        return airports.findAll();
    }

    /**
     * Ensures a new flight may be scheduled on the route for the given departure date (US074 consumer rule).
     */
    public void assertRouteAcceptsNewFlight(final String routeName, final LocalDate flightDepartureDate) {
        if (flightDepartureDate == null) {
            throw new IllegalArgumentException("Flight departure date is required.");
        }
        final RouteName normalizedName = RouteName.valueOf(routeName);
        final Route route = routes.ofIdentity(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeName));
        route.assertUsableForFlight(flightDepartureDate.atStartOfDay());
    }

    private Airport requireAirport(final String iataCode) {
        return airports.ofIdentity(AirportIATACode.valueOf(iataCode))
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + iataCode));
    }

    private void assertUniqueRouteName(final RouteName routeName) {
        if (routes.existsByName(routeName)) {
            throw new RouteAlreadyExistsException("Route name already exists.");
        }
    }

    private void assertRouteNameBelongsToCompany(final RouteName routeName,
                                                 final AirTransportCompany airTransportCompany) {
        final String prefix = airTransportCompany.identity().toString();
        if (!routeName.toString().startsWith(prefix)) {
            throw new IllegalArgumentException("Route name must use company IATA prefix " + prefix + ".");
        }
    }
}
