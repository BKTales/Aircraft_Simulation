package eapli.aisafe.routemanagement.application;

import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.DeactivateRouteTestRepositories;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeactivateRouteServiceTest {

    private RouteRepository routes;
    private FlightRepository flights;
    private DeactivateRouteService service;

    @BeforeEach
    void setUp() {
        routes = DeactivateRouteTestRepositories.newRouteRepository();
        flights = DeactivateRouteTestRepositories.newFlightRepository();
        service = new DeactivateRouteService(routes, flights);
    }

    @Test
    void deactivateSucceedsWhenNoBlockingFlights() {
        final Route route = saveActiveRoute("TP100");

        final Route deactivated = service.deactivateRoute(
                "TP100", LocalDate.now().plusDays(30), RouteTestFixtures.COMPANY_TP);

        assertEquals(LocalDate.now().plusDays(30), deactivated.deactivationDate().value());
        assertTrue(routes.ofIdentity(RouteName.valueOf("TP100")).orElseThrow().deactivationDate() != null);
    }

    @Test
    void deactivateFailsWhenRouteNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deactivateRoute("TP999", LocalDate.now().plusDays(1), RouteTestFixtures.COMPANY_TP));
    }

    @Test
    void deactivateFailsWhenWrongCompany() {
        saveActiveRoute("TP100");
        assertThrows(IllegalArgumentException.class,
                () -> service.deactivateRoute(
                        "TP100", LocalDate.now().plusDays(1), RouteTestFixtures.company("FR", "FR", "FRA")));
    }

    @Test
    void deactivateFailsWhenAlreadyDeactivated() {
        final Route route = saveActiveRoute("TP100");
        route.deactivate(eapli.aisafe.routemanagement.domain.DeactivationDate.valueOf(LocalDate.now().plusDays(5)));
        routes.save(route);

        assertThrows(RouteAlreadyDeactivatedException.class,
                () -> service.deactivateRoute("TP100", LocalDate.now().plusDays(10), RouteTestFixtures.COMPANY_TP));
    }

    @Test
    void deactivateFailsWhenDateInPast() {
        saveActiveRoute("TP100");
        assertThrows(IllegalArgumentException.class,
                () -> service.deactivateRoute("TP100", LocalDate.now().minusDays(1), RouteTestFixtures.COMPANY_TP));
    }

    @Test
    void deactivateFailsWhenDraftFutureFlightExists() {
        saveActiveRoute("TP100");
        flights.save(futureFlight("TP100", FlightPlanStatus.DRAFT, LocalDate.now().plusDays(40)));

        assertThrows(RouteHasPlannedFlightsException.class,
                () -> service.deactivateRoute("TP100", LocalDate.now().plusDays(30), RouteTestFixtures.COMPANY_TP));
    }

    @Test
    void deactivateSucceedsWhenOnlySimRejectedFutureFlight() {
        saveActiveRoute("TP100");
        flights.save(futureFlight("TP100", FlightPlanStatus.SIM_REJECTED, LocalDate.now().plusDays(40)));

        final Route result = service.deactivateRoute(
                "TP100", LocalDate.now().plusDays(30), RouteTestFixtures.COMPANY_TP);
        assertEquals(LocalDate.now().plusDays(30), result.deactivationDate().value());
    }

    @Test
    void deactivateFailsWhenScheduledFlightWithoutPlanExists() {
        saveActiveRoute("TP100");
        final Flight flight = new Flight(
                new FlightDesignator("TPF001"), "TP100", "CS-TST");
        flight.assignSchedule(new FlightSchedule(
                LocalDate.now().plusDays(40).atStartOfDay(),
                LocalDate.now().plusDays(40).atTime(12, 0)));
        flights.save(flight);

        assertThrows(RouteHasPlannedFlightsException.class,
                () -> service.deactivateRoute("TP100", LocalDate.now().plusDays(30), RouteTestFixtures.COMPANY_TP));
    }

    @Test
    void deactivateSucceedsWhenOnlyPastFlightOnRoute() {
        saveActiveRoute("TP100");
        flights.save(futureFlight("TP100", FlightPlanStatus.DRAFT, LocalDate.now().minusDays(5)));

        final Route result = service.deactivateRoute(
                "TP100", LocalDate.now().plusDays(1), RouteTestFixtures.COMPANY_TP);
        assertTrue(result.deactivationDate() != null);
    }

    @Test
    void listActiveRouteOptionsIncludesLastPlannedFlightDate() {
        saveActiveRoute("TP1001");
        final LocalDate futureDeparture = LocalDate.now().plusDays(45);
        flights.save(futureFlight("TP1001", FlightPlanStatus.DRAFT, futureDeparture));

        final List<ActiveRouteOption> options = service.listActiveRouteOptionsByCompany(RouteTestFixtures.COMPANY_TP);

        assertEquals(1, options.size());
        assertEquals(futureDeparture, options.get(0).lastPlannedFlightDeparture().orElseThrow());
    }

    @Test
    void listActiveRouteOptionsIgnoresPastPlannedFlights() {
        saveActiveRoute("TP1001");
        flights.save(futureFlight("TP1001", FlightPlanStatus.DRAFT, LocalDate.now().minusDays(10)));

        final List<ActiveRouteOption> options = service.listActiveRouteOptionsByCompany(RouteTestFixtures.COMPANY_TP);

        assertTrue(options.get(0).lastPlannedFlightDeparture().isEmpty());
    }

    @Test
    void listActiveRouteOptionsShowsEmptyWhenNoPlannedFlights() {
        saveActiveRoute("TP1001");

        final List<ActiveRouteOption> options = service.listActiveRouteOptionsByCompany(RouteTestFixtures.COMPANY_TP);

        assertTrue(options.get(0).lastPlannedFlightDeparture().isEmpty());
    }

    @Test
    void listActiveRoutesExcludesDeactivated() {
        saveActiveRoute("TP100");
        final Route inactive = saveActiveRoute("TP200");
        inactive.deactivate(eapli.aisafe.routemanagement.domain.DeactivationDate.valueOf(LocalDate.now().plusDays(1)));
        routes.save(inactive);

        assertEquals(1, service.listActiveRoutesByCompany(RouteTestFixtures.COMPANY_TP).size());
        assertEquals("TP100", service.listActiveRoutesByCompany(RouteTestFixtures.COMPANY_TP).get(0).identity().toString());
    }

    private Route saveActiveRoute(final String name) {
        final Route route = Route.regularRoute(
                RouteName.valueOf(name),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        return routes.save(route);
    }

    private static Flight futureFlight(final String routeName,
                                       final FlightPlanStatus status,
                                       final LocalDate departureDay) {
        final FlightDesignator designator = new FlightDesignator("TP" + routeName + status.name().charAt(0));
        final Flight flight = new Flight(designator, routeName, "CS-TST");
        final LocalDateTime departure = departureDay.atTime(10, 0);
        flight.assignSchedule(new FlightSchedule(departure, departure.plusHours(2)));
        flight.assignFlightPlan(FlightPlan.forFlight(
                designator, status, FuelLoad.valueOf(1000.0), null, "{\"ok\":true}"));
        return flight;
    }

}
