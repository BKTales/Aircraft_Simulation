package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;
import eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.routemanagement.domain.Route;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightPlanAreaClipperTest {

    private static final Airport AIRPORT_FNC = new Airport(
            AirportIATACode.valueOf("FNC"),
            AirportICAOCode.valueOf("LPMA"),
            Coordinates.valueOf(32.6949, -16.7745, 58.0),
            AreaCode.valueOf("AREA-3"));

    private final RouteAreaCrossingDetector detector = new RouteAreaCrossingDetector();
    private final FlightPlanAreaClipper clipper = new FlightPlanAreaClipper();

    @Test
    void ensurePartialClipReducesFuelAndUsesSyntheticEndpoints() {
        final GeographicBoundary narrowLisboa = bootstrapLisboaBoundary();
        final FlightPlanDescriptor full = directPlan(AIRPORT_LIS, AIRPORT_FNC);
        final AreaCrossingSpan span = detector.findCrossingSpan(full, narrowLisboa).orElseThrow();
        assertFalse(span.isFullLeg());
        final FlightPlanDescriptor clipped = clipper.clip(full, span);

        final var leg = clipped.getLegs().get(0);
        assertTrue(leg.getFuelValue() < full.getLegs().get(0).getFuelValue());
        assertEquals("LIS", leg.getDepartureAirport());
        assertEquals("EXT", leg.getArrivalAirport());
        assertTrue(leg.getRoute().segments().size() <= full.getLegs().get(0).getRoute().segments().size());
    }

    @Test
    void ensureFullLegClipReturnsSameDescriptor() {
        final FlightPlanDescriptor full = directPlan(AIRPORT_OPO, AIRPORT_LIS);
        final AreaCrossingSpan span = detector.findCrossingSpan(full, lisboaBoundary()).orElseThrow();
        assertEquals(full, clipper.clip(full, span));
    }

    private static GeographicBoundary bootstrapLisboaBoundary() {
        return GeographicBoundary.valueOf(List.of(
                GeographicCoords.valueOf(36.4f, -10.1f),
                GeographicCoords.valueOf(39.4f, -10.1f),
                GeographicCoords.valueOf(39.4f, -6.4f),
                GeographicCoords.valueOf(36.4f, -6.4f)));
    }

    private static GeographicBoundary lisboaBoundary() {
        return GeographicBoundary.valueOf(List.of(
                GeographicCoords.valueOf(32.0f, -17.5f),
                GeographicCoords.valueOf(42.5f, -17.5f),
                GeographicCoords.valueOf(42.5f, -6.0f),
                GeographicCoords.valueOf(32.0f, -6.0f)));
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
