package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.api.FlightPlanJsonExporter;
import eapli.aisafe.dsl.api.SimulatorFlightPlanJsonContext;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.routemanagement.domain.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Builds a single-leg {@link FlightPlanDescriptor} and simulator JSON for a direct route (US080).
 */
public final class DirectRouteFlightPlanComposer {

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private static final int CRUISE_ALTITUDE_METRES = 11_000;
    private static final int SLOT_WIDTH_METRES = 500;
    private static final int DEFAULT_WIND_DIRECTION = 90;
    private static final double DEFAULT_WIND_SPEED = 10.5;

    private DirectRouteFlightPlanComposer() {}

    public record ComposedPlan(FlightPlanDescriptor descriptor, String jsonContent) {}

    public static ComposedPlan compose(final FlightDesignator designator,
                                       final Route route,
                                       final Airport origin,
                                       final Airport destination,
                                       final LocalDateTime departure,
                                       final LocalDateTime arrival,
                                       final FuelQuantity fuel,
                                       final int passengerCount,
                                       final double passengerWeightKg,
                                       final double cargoWeightKg,
                                       final String aircraftModelId,
                                       final AircraftModel aircraftModel,
                                       final EngineModel engineModel) {
        if (aircraftModel == null || engineModel == null) {
            throw new IllegalArgumentException("Aircraft model and engine are required.");
        }
        final FlightPlanDescriptor descriptor = buildDescriptor(
                designator, route, origin, destination, departure, arrival, fuel,
                passengerCount, passengerWeightKg, cargoWeightKg);
        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId(aircraftModelId)
                .aircraftModel(aircraftModel)
                .engineModel(engineModel)
                .airport(origin)
                .airport(destination)
                .build();
        final String json = FlightPlanJsonExporter.toSimulatorJson(descriptor, context);
        return new ComposedPlan(descriptor, json);
    }

    public static FlightPlanDescriptor buildDescriptor(final FlightDesignator designator,
                                                final Route route,
                                                final Airport origin,
                                                final Airport destination,
                                                final LocalDateTime departure,
                                                final LocalDateTime arrival,
                                                final FuelQuantity fuel,
                                                final int passengerCount,
                                                final double passengerWeightKg,
                                                final double cargoWeightKg) {
        if (designator == null || route == null || origin == null || destination == null
                || departure == null || arrival == null || fuel == null) {
            throw new IllegalArgumentException("Required composition fields are missing.");
        }
        final String depTime = departure.format(DT);
        final String arrTime = arrival.format(DT);
        final String originIata = origin.identity().toString();
        final String destIata = destination.identity().toString();

        final RouteDescriptor routeDescriptor = new RouteDescriptor(
                defaultSegments(origin.coordinates(), destination.coordinates()));

        final LegDescriptor leg = new LegDescriptor(
                originIata, depTime,
                destIata, arrTime,
                routeDescriptor,
                fuel.amount(), fuel.unit());

        final String flightType = route.flightType().name();
        return new FlightPlanDescriptor(
                designator.toString(),
                flightType,
                passengerCount,
                passengerWeightKg,
                cargoWeightKg,
                List.of(leg));
    }

    private static List<SegmentDescriptor> defaultSegments(final Coordinates origin, final Coordinates destination) {
        final double startLat = origin.latitude();
        final double startLon = origin.longitude();
        final double endLat = destination.latitude();
        final double endLon = destination.longitude();
        final double startAlt = origin.elevationMeters();
        final double endAlt = destination.elevationMeters();

        final double mid1Lat = startLat + (endLat - startLat) * 0.33;
        final double mid1Lon = startLon + (endLon - startLon) * 0.33;
        final double mid2Lat = startLat + (endLat - startLat) * 0.66;
        final double mid2Lon = startLon + (endLon - startLon) * 0.66;

        final AltitudeSlotDescriptor cruiseSlot = new AltitudeSlotDescriptor(CRUISE_ALTITUDE_METRES, SLOT_WIDTH_METRES);
        final AltitudeSlotDescriptor groundSlot = new AltitudeSlotDescriptor(
                (int) Math.max(1, Math.round(startAlt)), SLOT_WIDTH_METRES);

        final SegmentDescriptor climb = new SegmentDescriptor(
                startLat, startLon, mid1Lat, mid1Lon,
                List.of(groundSlot, cruiseSlot),
                DEFAULT_WIND_DIRECTION, DEFAULT_WIND_SPEED);

        final SegmentDescriptor cruise = new SegmentDescriptor(
                mid1Lat, mid1Lon, mid2Lat, mid2Lon,
                List.of(cruiseSlot),
                DEFAULT_WIND_DIRECTION, DEFAULT_WIND_SPEED);

        final AltitudeSlotDescriptor destGroundSlot = new AltitudeSlotDescriptor(
                (int) Math.max(1, Math.round(endAlt)), SLOT_WIDTH_METRES);

        final SegmentDescriptor descend = new SegmentDescriptor(
                mid2Lat, mid2Lon, endLat, endLon,
                List.of(cruiseSlot, destGroundSlot),
                DEFAULT_WIND_DIRECTION, DEFAULT_WIND_SPEED);

        return List.of(climb, cruise, descend);
    }
}
