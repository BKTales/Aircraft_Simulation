package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.domain.DeactivationDate;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RouteServiceDeactivateConsumerTest {

    private RouteRepository routes;
    private RouteService service;

    @BeforeEach
    void setUp() {
        routes = mock(RouteRepository.class);
        service = new RouteService(routes, mock(AirportRepository.class));
    }

    @Test
    void assertRouteAcceptsNewFlightWhenRouteActive() {
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        org.mockito.Mockito.when(routes.ofIdentity(RouteTestFixtures.ROUTE_NAME_TP123))
                .thenReturn(java.util.Optional.of(route));

        assertDoesNotThrow(() -> service.assertRouteAcceptsNewFlight(
                "TP123", LocalDate.of(2026, 6, 1)));
    }

    @Test
    void assertRouteAcceptsNewFlightFailsWhenDeactivatedOnSameDay() {
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        route.deactivate(DeactivationDate.valueOf(LocalDate.of(2026, 6, 15)));
        org.mockito.Mockito.when(routes.ofIdentity(RouteTestFixtures.ROUTE_NAME_TP123))
                .thenReturn(java.util.Optional.of(route));

        assertThrows(IllegalStateException.class,
                () -> service.assertRouteAcceptsNewFlight("TP123", LocalDate.of(2026, 6, 15)));
    }

    @Test
    void assertRouteAcceptsNewFlightSucceedsDayBeforeDeactivation() {
        final Route route = Route.regularRoute(
                RouteTestFixtures.ROUTE_NAME_TP123,
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        route.deactivate(DeactivationDate.valueOf(LocalDate.of(2026, 6, 15)));
        org.mockito.Mockito.when(routes.ofIdentity(RouteTestFixtures.ROUTE_NAME_TP123))
                .thenReturn(java.util.Optional.of(route));

        assertDoesNotThrow(() -> service.assertRouteAcceptsNewFlight(
                "TP123", LocalDate.of(2026, 6, 14)));
    }
}
