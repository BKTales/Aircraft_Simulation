package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static eapli.aisafe.routemanagement.RouteTestFixtures.mondaySchedule;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteAvailabilityTest {

    @Test
    void isActiveOnWhenNotDeactivated() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());
        assertTrue(route.isActiveOn(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void isActiveOnFalseOnOrAfterDeactivation() {
        final Route route = Route.regularRouteDeactivated(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS,
                mondaySchedule(), LocalDate.of(2026, 6, 1));
        assertFalse(route.isActiveOn(LocalDate.of(2026, 6, 1)));
        assertTrue(route.isActiveOn(LocalDate.of(2026, 5, 31)));
    }

    @Test
    void charterDepartureMustBeWithinWindow() {
        final Route route = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule());
        route.assertDepartureMatchesSchedule(LocalDateTime.of(2026, 6, 1, 10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> route.assertDepartureMatchesSchedule(LocalDateTime.of(2026, 6, 3, 10, 0)));
    }

    @Test
    void regularDepartureMustMatchRecurringDay() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());
        route.assertDepartureMatchesSchedule(LocalDateTime.of(2026, 6, 1, 8, 0));
        assertThrows(IllegalArgumentException.class,
                () -> route.assertDepartureMatchesSchedule(LocalDateTime.of(2026, 6, 2, 8, 0)));
    }
}
