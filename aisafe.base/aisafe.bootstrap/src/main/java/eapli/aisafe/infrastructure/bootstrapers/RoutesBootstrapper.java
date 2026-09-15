package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.routemanagement.domain.DeactivationDate;
import eapli.aisafe.routemanagement.domain.RecurringScheduleEntry;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.domain.RouteRecurringSchedule;
import eapli.aisafe.routemanagement.domain.RouteSchedule;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds demo routes (US073) and US074 deactivation demo routes for company TP.
 */
public class RoutesBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesBootstrapper.class);

    @Override
    public boolean execute() {
        final RouteRepository routes = PersistenceContext.repositories().routes();
        final AirTransportCompanyRepository companies = PersistenceContext.repositories().airTransportCompanies();
        final AirportRepository airports = PersistenceContext.repositories().airports();

        try {
            saveRegular(routes, companies, airports, "TP1001", "TP", "OPO", "LIS", List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY));
            saveRegular(routes, companies, airports, "FR2001", "FR", "LIS", "FAO", List.of(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY));
            saveRegular(routes, companies, airports, "LH3001", "LH", "FAO", "OPO", List.of(DayOfWeek.WEDNESDAY));

            final LocalDate now = LocalDate.now();
            saveCharter(routes, companies, airports, "VY4001", "VY", "OPO", "FAO", now.plusDays(10), now.plusDays(12));
            saveCharter(routes, companies, airports, "IB5001", "IB", "LIS", "OPO", now.plusDays(20), now.plusDays(22));

            saveCharter(routes, companies, airports, "TP1002", "TP", "LIS", "FNC",
                    now.plusMonths(1), now.plusMonths(1).plusDays(2));

            final LocalDate us100PassDate = FlightBootstrapper.nextUs100PassDeparture().toLocalDate();
            saveCharter(routes, companies, airports, "TP1003", "TP", "LIS", "FAO",
                    us100PassDate, us100PassDate.plusDays(2));
            saveCharter(routes, companies, airports, "TP1004", "TP", "FAO", "LIS",
                    us100PassDate, us100PassDate.plusDays(2));
            saveCharter(routes, companies, airports, "TP1005", "TP", "LIS", "FNC",
                    us100PassDate, us100PassDate.plusDays(2));
            saveCharter(routes, companies, airports, "TP1006", "TP", "OPO", "LIS",
                    us100PassDate, us100PassDate.plusDays(2));

            final LocalDate us085ClientDate = FlightBootstrapper.nextUs085ClientDeparture().toLocalDate();
            saveCharter(routes, companies, airports, "AA123", "TP", "OPO", "MAD",
                    us085ClientDate, us085ClientDate.plusDays(2));

            // US074 manual test routes (TP collaborator / atcc1) — names avoid US100 charter routes above
            saveRegular(routes, companies, airports, "TP7401", "TP", "OPO", "LIS", List.of(DayOfWeek.TUESDAY));
            saveRegular(routes, companies, airports, "TP7402", "TP", "OPO", "FAO", List.of(DayOfWeek.WEDNESDAY));
            saveCharter(routes, companies, airports, "TP7403", "TP", "OPO", "FAO", now.plusDays(15), now.plusDays(17));
            saveDeactivated(routes, companies, airports, "TP7404", "TP", "LIS", "OPO", now.plusDays(60));
        } catch (final RuntimeException ex) {
            LOGGER.warn("Could not bootstrap routes: {}", ex.getMessage());
            LOGGER.trace("Route bootstrap failure", ex);
        }
        return true;
    }

    private static void saveRegular(final RouteRepository routes,
                                    final AirTransportCompanyRepository companies,
                                    final AirportRepository airports,
                                    final String routeName,
                                    final String companyIata,
                                    final String originIata,
                                    final String destinationIata,
                                    final List<DayOfWeek> days) {
        final RouteName id = RouteName.valueOf(routeName);
        if (routes.ofIdentity(id).isPresent()) {
            return;
        }
        final RouteRecurringSchedule recurringSchedule = new RouteRecurringSchedule(
                days.stream().map(RecurringScheduleEntry::of).toList());
        final Route route = Route.regularRoute(
                id,
                requireCompany(companies, companyIata),
                requireAirport(airports, originIata),
                requireAirport(airports, destinationIata),
                recurringSchedule);
        routes.save(route);
    }

    private static void saveDeactivated(final RouteRepository routes,
                                        final AirTransportCompanyRepository companies,
                                        final AirportRepository airports,
                                        final String routeName,
                                        final String companyIata,
                                        final String originIata,
                                        final String destinationIata,
                                        final LocalDate deactivationDate) {
        final RouteName id = RouteName.valueOf(routeName);
        if (routes.ofIdentity(id).isPresent()) {
            return;
        }
        final Route route = Route.regularRoute(
                id,
                requireCompany(companies, companyIata),
                requireAirport(airports, originIata),
                requireAirport(airports, destinationIata),
                new RouteRecurringSchedule(List.of(RecurringScheduleEntry.of(DayOfWeek.FRIDAY))));
        route.deactivate(DeactivationDate.valueOf(deactivationDate));
        routes.save(route);
    }

    private static void saveCharter(final RouteRepository routes,
                                    final AirTransportCompanyRepository companies,
                                    final AirportRepository airports,
                                    final String routeName,
                                    final String companyIata,
                                    final String originIata,
                                    final String destinationIata,
                                    final LocalDate departure,
                                    final LocalDate arrival) {
        final RouteName id = RouteName.valueOf(routeName);
        if (routes.ofIdentity(id).isPresent()) {
            return;
        }
        final Route route = Route.charterRoute(
                id,
                requireCompany(companies, companyIata),
                requireAirport(airports, originIata),
                requireAirport(airports, destinationIata),
                new RouteSchedule(departure, arrival));
        routes.save(route);
    }

    private static AirTransportCompany requireCompany(final AirTransportCompanyRepository companies,
                                                      final String companyIata) {
        return companies.ofIdentity(IATACode.valueOf(companyIata))
                .orElseThrow(() -> new IllegalStateException("Company not found: " + companyIata));
    }

    private static Airport requireAirport(final AirportRepository airports, final String iata) {
        return airports.ofIdentity(AirportIATACode.valueOf(iata))
                .orElseThrow(() -> new IllegalStateException("Airport not found: " + iata));
    }
}
