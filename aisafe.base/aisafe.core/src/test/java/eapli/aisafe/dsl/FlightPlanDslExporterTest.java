package eapli.aisafe.dsl;

import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.FlightPlanDslExporter;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.application.DirectRouteFlightPlanComposer;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.routemanagement.domain.Route;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_LIS;
import static eapli.aisafe.routemanagement.RouteTestFixtures.AIRPORT_OPO;
import static eapli.aisafe.routemanagement.RouteTestFixtures.COMPANY_TP;
import static eapli.aisafe.routemanagement.RouteTestFixtures.ROUTE_NAME_TP123;
import static eapli.aisafe.routemanagement.RouteTestFixtures.charterSchedule;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightPlanDslExporterTest {

    private final FlightDslParser parser = new FlightDslParser();

    @Test
    void exportedDirectRoutePlanRoundTripsThroughParser() {
        final Route route = Route.charterRoute(
                ROUTE_NAME_TP123, COMPANY_TP, AIRPORT_OPO, AIRPORT_LIS, charterSchedule());
        final FlightDesignator designator = FlightDesignator.fromRoute(route.identity(), java.util.Optional.empty());

        final DirectRouteFlightPlanComposer.ComposedPlan composed = DirectRouteFlightPlanComposer.compose(
                designator,
                route,
                route.originAirport(),
                route.destinationAirport(),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                FuelQuantity.kilograms(5000),
                120,
                10000,
                500,
                SimulatorJsonTestFixtures.MODEL_ID.toString(),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine());

        final String dsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        assertTrue(dsl.contains("flight TP123"));
        assertTrue(dsl.contains("type CHARTER"));
        assertTrue(dsl.contains("departure OPO"));
        assertTrue(dsl.contains("arrival LIS"));

        final ParseResult result = parser.parse(dsl);
        assertTrue(result.isValid(), () -> String.join("; ", result.getErrors()));
    }

    @Test
    void invalidArrivalTimeFailsSemanticValidationAfterExport() {
        final SegmentDescriptor segment = new SegmentDescriptor(
                38.77, -9.13, 40.47, -3.57,
                List.of(new AltitudeSlotDescriptor(11000, 500)),
                90, 10.5);
        final LegDescriptor leg = new LegDescriptor(
                "OPO", "2026-06-01 12:00",
                "LIS", "2026-06-01 10:00",
                new RouteDescriptor(List.of(segment)),
                5000, "kg");
        final FlightPlanDescriptor descriptor = new FlightPlanDescriptor("TP123", "CHARTER", 0, 0.0, List.of(leg));

        final ParseResult result = parser.parse(FlightPlanDslExporter.toDsl(descriptor));
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Arrival time")));
    }
}
