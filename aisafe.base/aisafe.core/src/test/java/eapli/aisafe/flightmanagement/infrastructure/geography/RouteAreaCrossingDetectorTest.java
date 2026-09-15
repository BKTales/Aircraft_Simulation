package eapli.aisafe.flightmanagement.infrastructure.geography;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.flightmanagement.application.DirectRouteFlightPlanComposer;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;
import eapli.aisafe.routemanagement.domain.Route;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteAreaCrossingDetectorTest {

    private static final Airport AIRPORT_FNC = new Airport(
            AirportIATACode.valueOf("FNC"),
            AirportICAOCode.valueOf("LPMA"),
            Coordinates.valueOf(32.6949, -16.7745, 58.0),
            AreaCode.valueOf("AREA-3"));

    private static final Airport AIRPORT_PDL = new Airport(
            AirportIATACode.valueOf("PDL"),
            AirportICAOCode.valueOf("LPPD"),
            Coordinates.valueOf(37.7412, -25.6979, 79.0),
            AreaCode.valueOf("AREA-4"));

    private final RouteAreaCrossingDetector detector = new RouteAreaCrossingDetector();

    /** Mainland band wide enough to include OPO, LIS and the OPO→FNC great-circle path. */
    private static GeographicBoundary lisboaBoundary() {
        return GeographicBoundary.valueOf(List.of(
                GeographicCoords.valueOf(32.0f, -17.5f),
                GeographicCoords.valueOf(42.5f, -17.5f),
                GeographicCoords.valueOf(42.5f, -6.0f),
                GeographicCoords.valueOf(32.0f, -6.0f)));
    }

    @Test
    void ensureOpOToLisCrossesLisboaArea() {
        final FlightPlanDescriptor plan = directPlan(AIRPORT_OPO, AIRPORT_LIS);
        assertTrue(detector.crossesArea(plan, lisboaBoundary()));
        final Optional<AreaCrossingSpan> span = detector.findCrossingSpan(plan, lisboaBoundary());
        assertTrue(span.isPresent());
        assertTrue(span.get().isFullLeg());
    }

    @Test
    void ensureLisToFncCrossesLisboaPartially() {
        final GeographicBoundary narrowLisboa = bootstrapLisboaBoundary();
        final FlightPlanDescriptor plan = directPlan(AIRPORT_LIS, AIRPORT_FNC);
        assertTrue(detector.crossesArea(plan, narrowLisboa));
        final Optional<AreaCrossingSpan> span = detector.findCrossingSpan(plan, narrowLisboa);
        assertTrue(span.isPresent());
        assertFalse(span.get().isFullLeg());
        assertTrue(span.get().exitFraction() < 0.95);
    }

    @Test
    void ensureAzoresRouteDoesNotCrossLisboa() {
        final FlightPlanDescriptor plan = directPlan(AIRPORT_FNC, AIRPORT_PDL);
        assertFalse(detector.crossesArea(plan, bootstrapLisboaBoundary()));
    }

    private static GeographicBoundary bootstrapLisboaBoundary() {
        return GeographicBoundary.valueOf(List.of(
                GeographicCoords.valueOf(36.4f, -10.1f),
                GeographicCoords.valueOf(39.4f, -10.1f),
                GeographicCoords.valueOf(39.4f, -6.4f),
                GeographicCoords.valueOf(36.4f, -6.4f)));
    }

    private static FlightPlanDescriptor directPlan(final Airport origin, final Airport destination) {
        final Route route = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, origin, destination, charterSchedule());
        return DirectRouteFlightPlanComposer.buildDescriptor(
                new FlightDesignator("TP999"),
                route,
                origin,
                destination,
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 12, 0),
                FuelQuantity.kilograms(12000),
                100, 8000, 500);
    }
}
