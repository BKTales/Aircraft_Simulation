package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.FlightPlanDslExporter;
import eapli.aisafe.flightmanagement.application.DirectRouteFlightPlanComposer;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector;
import eapli.aisafe.routemanagement.domain.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightEligibilityServiceTest {

    private static final String AREA_LISBOA = "AREA-0";

    private AirControlAreaRepository areaRepository;
    private FlightEligibilityService service;

    @BeforeEach
    void setUp() {
        areaRepository = mock(AirControlAreaRepository.class);
        when(areaRepository.ofIdentity(any(AreaCode.class))).thenReturn(Optional.of(lisboaArea()));
        service = new FlightEligibilityService(areaRepository, new FlightDslParser(), new RouteAreaCrossingDetector());
    }

    @Test
    void ensureEligibleFlightMatchesAreaAndInterval() {
        final Flight flight = flightWithDsl(AIRPORT_OPO, AIRPORT_LIS);
        assertTrue(service.isEligible(
                flight,
                AREA_LISBOA,
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 13, 0)));
        assertEquals(AreaClipMode.CLIPPED, service.clipModeFor(flight, AREA_LISBOA).orElseThrow());
    }

    @Test
    void ensureFlightOutsideIntervalIsNotEligible() {
        final Flight flight = flightWithDsl(AIRPORT_OPO, AIRPORT_LIS);
        assertFalse(service.isEligible(
                flight,
                AREA_LISBOA,
                LocalDateTime.of(2026, 6, 2, 9, 0),
                LocalDateTime.of(2026, 6, 2, 13, 0)));
    }

    @Test
    void ensureTransitFlightIsEligibleWithClippedMode() {
        final Airport fnc = airport("FNC", "LPMA", 32.6949, -16.7745);
        final Flight flight = flightWithDsl(AIRPORT_LIS, fnc);
        assertTrue(service.isEligible(
                flight,
                AREA_LISBOA,
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 13, 0)));
        assertEquals(AreaClipMode.CLIPPED, service.clipModeFor(flight, AREA_LISBOA).orElseThrow());
    }

    @Test
    void ensureFilterEligibleReturnsMatchingFlights() {
        final Flight eligible = flightWithDsl(AIRPORT_OPO, AIRPORT_LIS);
        final List<Flight> result = service.filterEligible(
                List.of(eligible),
                AREA_LISBOA,
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 13, 0));
        assertEquals(1, result.size());
    }

    private static Flight flightWithDsl(final Airport origin, final Airport destination) {
        final Route route = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, origin, destination, charterSchedule());
        final var descriptor = DirectRouteFlightPlanComposer.buildDescriptor(
                new FlightDesignator("TP100"),
                route,
                origin,
                destination,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                FuelQuantity.kilograms(12000),
                100, 8000, 500);
        final String dsl = FlightPlanDslExporter.toDsl(descriptor);
        final Flight flight = new Flight(
                new FlightDesignator("TP100"),
                "ROUTE-1",
                "CS-DEMO");
        flight.assignSchedule(new FlightSchedule(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0)));
        flight.assignFlightPlan(FlightPlan.forFlight(
                flight.designator(),
                FlightPlanStatus.DRAFT,
                eapli.aisafe.flightmanagement.domain.FuelLoad.valueOf(12000),
                dsl,
                "{\"placeholder\":true}"));
        return flight;
    }

    private static AirControlArea lisboaArea() {
        return new AirControlArea(
                new AirControlAreaName("TMA Lisboa"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(36.4f, -10.1f),
                        GeographicCoords.valueOf(39.4f, -10.1f),
                        GeographicCoords.valueOf(39.4f, -6.4f),
                        GeographicCoords.valueOf(36.4f, -6.4f))),
                MinFuelRequirement.valueOf(2000f));
    }

    private static Airport airport(final String iata, final String icao,
                                   final double lat, final double lon) {
        return new Airport(
                AirportIATACode.valueOf(iata),
                AirportICAOCode.valueOf(icao),
                Coordinates.valueOf(lat, lon, 50.0),
                AreaCode.valueOf("AREA-0"));
    }
}
