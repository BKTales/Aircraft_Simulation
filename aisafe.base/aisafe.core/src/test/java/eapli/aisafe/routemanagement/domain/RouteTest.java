package eapli.aisafe.routemanagement.domain;

import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static eapli.aisafe.routemanagement.RouteTestFixtures.mondaySchedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteTest {

    @Test
    void charterRouteCreatesWithSchedule() {
        final Route route = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule());

        assertEquals(ROUTE_NAME_TP123, route.identity());
        assertEquals(FlightType.CHARTER, route.flightType());
        assertEquals(COMPANY_TP, route.airTransportCompany());
        assertEquals(AIRPORT_OPO, route.originAirport());
        assertEquals(AIRPORT_LIS, route.destinationAirport());
        assertEquals("TP", route.companyIATACode().toString());
        assertEquals("OPO", route.originAirportIATACode().toString());
        assertEquals("LIS", route.destinationAirportIATACode().toString());
        assertNull(route.routeRecurringSchedule());
        assertNull(route.deactivationDate());
    }

    @Test
    void regularRouteCreatesWithRecurringSchedule() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());

        assertEquals(FlightType.REGULAR, route.flightType());
        assertNull(route.routeSchedule());
        assertEquals(1, route.routeRecurringSchedule().entries().size());
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(null, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule()));
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(ROUTE_NAME_TP123, null, AIRPORT_OPO, AIRPORT_LIS, charterSchedule()));
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(ROUTE_NAME_TP123, COMPANY_TP, null, AIRPORT_LIS, charterSchedule()));
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, null, charterSchedule()));
    }

    @Test
    void rejectsEqualOriginAndDestination() {
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_OPO, charterSchedule()));
    }

    @Test
    void rejectsCharterWithoutSchedule() {
        assertThrows(IllegalArgumentException.class,
                () -> Route.charterRoute(ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, null));
    }

    @Test
    void rejectsRegularWithoutRecurringSchedule() {
        assertThrows(IllegalArgumentException.class,
                () -> Route.regularRoute(ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, null));
    }

    @Test
    void sameAsComparesByIdentity() {
        final Route a = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule());
        final Route b = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_LIS, AIRPORT_OPO,
                new RouteSchedule(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2)));

        assertTrue(a.sameAs(b));
        assertFalse(a.sameAs(Route.regularRoute(
                RouteName.valueOf("TP999"), COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule())));
    }

    @Test
    void deactivateSetsDateWhenActive() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());
        assertTrue(route.isActive());

        route.deactivate(DeactivationDate.valueOf(LocalDate.of(2026, 12, 31)));

        assertFalse(route.isActive());
        assertEquals(LocalDate.of(2026, 12, 31), route.deactivationDate().value());
    }

    @Test
    void deactivateFailsWhenAlreadyDeactivated() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());
        route.deactivate(DeactivationDate.valueOf(LocalDate.of(2026, 12, 31)));

        assertThrows(IllegalStateException.class,
                () -> route.deactivate(DeactivationDate.valueOf(LocalDate.of(2027, 1, 1))));
    }

    @Test
    void deactivateRejectsNullDate() {
        final Route route = Route.regularRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, mondaySchedule());

        assertThrows(IllegalArgumentException.class, () -> route.deactivate(null));
    }
}
