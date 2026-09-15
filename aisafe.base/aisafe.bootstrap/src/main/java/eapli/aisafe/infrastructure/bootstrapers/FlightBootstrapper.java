package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.dsl.api.FlightPlanDslExporter;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightmanagement.application.DirectRouteFlightPlanComposer;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Seeds US085 demo flights, US100 window flights, and US074 route deactivation demos.
 * TP085OK on CS-DEMO preserves the US071 pending-flight decommission demo.
 * AA123 loads the LAPR4 client fixture ({@code bootstrap/us085/Flight_Plan_LAPR4_A.json}).
 * After US085 validation, read elapsed_s and fuel_used_kg from the latest
 * report.csv under /tmp/aisafe-sim-reports-*.
 */
public class FlightBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightBootstrapper.class);

    private static final String ROUTE_NAME = "TP1001";
    private static final String PILOT_USERNAME = "pilot1";
    /** A320: fuelCapacity ~19296 kg; MTOW 78000 with ~10500 kg payload → leg fuel must stay well below tank capacity. */
    private static final FuelQuantity FUEL = FuelQuantity.kilograms(12000);
    private static final int PASSENGER_COUNT = 120;
    private static final double PASSENGER_WEIGHT_KG = 10000;
    private static final double CARGO_WEIGHT_KG = 500;

    private static final String US085_CLIENT_JSON_RESOURCE = "/bootstrap/us085/Flight_Plan_LAPR4_A.json";
    private static final FuelQuantity US085_CLIENT_FUEL = new FuelQuantity(39000, "l");

    private static final String PLACEHOLDER_JSON = """
            {
              "ID": "TP085DSL",
              "Type": "regular",
              "Route": "TP1001",
              "Leg": []
            }
            """;

    /** Leg fuel below OPO→LIS burn; kept self-contained so the simulator uses stored JSON (not DSL rebuild). */
    private static final FuelQuantity SIM_FAIL_LEG_FUEL = FuelQuantity.kilograms(500);

    @Override
    public boolean execute() {
        final var repos = PersistenceContext.repositories();
        try {
            final Route route = repos.routes().ofIdentity(RouteName.valueOf(ROUTE_NAME))
                    .orElseThrow(() -> new IllegalStateException("Route not found: " + ROUTE_NAME));
            final PilotUser pilot = repos.pilots().findByUsername(Username.valueOf(PILOT_USERNAME))
                    .orElseThrow(() -> new IllegalStateException("Pilot not found: " + PILOT_USERNAME));
            final Aircraft csDemo = requireAircraft(repos.aircraft(), "CS-DEMO");
            final Aircraft csTp02 = requireAircraft(repos.aircraft(), "CS-TP02");

            final LocalDateTime departure = nextTp1001Departure();
            final LocalDateTime arrival = departure.plusHours(2);
            final FlightSchedule schedule = new FlightSchedule(departure, arrival);

            seedHappyPath(repos.flights(), repos.aircraftModels(), repos.engineModels(),
                    route, pilot, csDemo, schedule);
            seedDslError(repos.flights(), repos.aircraftModels(), repos.engineModels(),
                    route, pilot, csTp02, schedule);
            seedSimulationError(repos.flights(), repos.aircraftModels(), repos.engineModels(),
                    route, pilot, csTp02, schedule);

            final Route aa123 = repos.routes().ofIdentity(RouteName.valueOf("AA123")).orElse(null);
            if (aa123 != null) {
                repos.aircraft().findByRegistration(AircraftRegistration.valueOf("CS-A380"))
                        .ifPresent(csA380 -> seedUs085ClientFixture(repos.flights(), aa123, pilot, csA380));
            }

            final Route clippedRoute = repos.routes().ofIdentity(RouteName.valueOf("TP1002"))
                    .orElse(null);
            if (clippedRoute != null) {
                seedClippedDemo(repos.flights(), repos.aircraftModels(), repos.engineModels(),
                        clippedRoute, pilot, csDemo, schedule);
            }

            seedUs100WindowFlights(repos, pilot, csDemo, csTp02);
            bootstrapUs074DemoFlights(repos.flights(), pilot, LocalDateTime.now());
        } catch (final RuntimeException ex) {
            LOGGER.warn("Could not bootstrap US085 demo flights (models/aircraft may be missing): {}", ex.getMessage());
            LOGGER.trace("Flight bootstrap failure", ex);
        }
        return true;
    }

    /**
     * US074 scenarios (login as TP collaborator atcc1):
     * <ul>
     *   <li>{@code TP1001} — past flight only → deactivation succeeds</li>
     *   <li>{@code TP7401} — future DRAFT (+ schedule-only) → deactivation blocked</li>
     *   <li>{@code TP7402} — future SIM_REJECTED only → deactivation succeeds</li>
     * </ul>
     */
    private static void bootstrapUs074DemoFlights(final FlightRepository flights,
                                                  final PilotUser pilot,
                                                  final LocalDateTime now) {
        final String json = PLACEHOLDER_JSON;

        savePlanned(flights, pilot, "TPU74P1", "TP1001", "CS-DEMO",
                new FlightSchedule(now.minusMonths(1), now.minusMonths(1).plusHours(2)),
                json, FlightPlanStatus.DRAFT);

        savePlanned(flights, pilot, "TPU74B1", "TP7401", "CS-DEMO",
                new FlightSchedule(now.plusMonths(2), now.plusMonths(2).plusHours(2)),
                json, FlightPlanStatus.DRAFT);

        saveScheduleOnly(flights, pilot, "TPU74B2", "TP7401", "CS-DEMO",
                new FlightSchedule(now.plusMonths(3), now.plusMonths(3).plusHours(2)));

        savePlanned(flights, pilot, "TPU74R1", "TP7402", "CS-DEMO",
                new FlightSchedule(now.plusMonths(2), now.plusMonths(2).plusHours(1)),
                json, FlightPlanStatus.SIM_REJECTED);

        savePlanned(flights, pilot, "TPU74C1", "TP7403", "CS-DEMO",
                new FlightSchedule(now.plusMonths(1), now.plusMonths(1).plusHours(3)),
                json, FlightPlanStatus.DRAFT);
    }

    private static void savePlanned(final FlightRepository flights,
                                    final PilotUser pilot,
                                    final String designator,
                                    final String routeName,
                                    final String aircraftRegistration,
                                    final FlightSchedule schedule,
                                    final String jsonContent,
                                    final FlightPlanStatus status) {
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            return;
        }
        final Flight flight = new Flight(
                new FlightDesignator(designator),
                routeName,
                aircraftRegistration);
        flight.assignSchedule(schedule);
        flight.assignPilotId(pilot);
        flight.assignFlightPlan(FlightPlan.forFlight(
                flight.designator(),
                status,
                FuelLoad.valueOf(12000.0),
                null,
                jsonContent));
        flights.save(flight);
    }

    private static void saveScheduleOnly(final FlightRepository flights,
                                         final PilotUser pilot,
                                         final String designator,
                                         final String routeName,
                                         final String aircraftRegistration,
                                         final FlightSchedule schedule) {
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            return;
        }
        final Flight flight = new Flight(
                new FlightDesignator(designator),
                routeName,
                aircraftRegistration);
        flight.assignSchedule(schedule);
        flight.assignPilotId(pilot);
        flights.save(flight);
    }

    private static void seedHappyPath(final FlightRepository flights,
                                      final AircraftModelRepository aircraftModels,
                                      final EngineModelRepository engineModels,
                                      final Route route,
                                      final PilotUser pilot,
                                      final Aircraft aircraft,
                                      final FlightSchedule schedule) {
        final String designator = "TP085OK";
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            return;
        }
        final FlightDesignator id = new FlightDesignator(designator);
        final DirectRouteFlightPlanComposer.ComposedPlan composed = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels);
        final String dsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                FUEL.toFuelLoad(),
                dsl,
                composed.jsonContent());
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
    }

    private static void seedDslError(final FlightRepository flights,
                                     final AircraftModelRepository aircraftModels,
                                     final EngineModelRepository engineModels,
                                     final Route route,
                                     final PilotUser pilot,
                                     final Aircraft aircraft,
                                     final FlightSchedule schedule) {
        final String designator = "TP085DSL";
        final FlightDesignator id = new FlightDesignator(designator);
        final DirectRouteFlightPlanComposer.ComposedPlan composed = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels);
        final String validDsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        final String invalidDsl = dslWithArrivalBeforeDeparture(validDsl);

        final var existing = flights.findByDesignator(id);
        if (existing.isPresent()) {
            final Flight flight = existing.get();
            if (isStaleDslErrorDemo(flight.flightPlan().dslContent(), invalidDsl)) {
                flight.flightPlan().replaceDraftContent(FUEL.toFuelLoad(), invalidDsl, PLACEHOLDER_JSON);
                flights.save(flight);
                LOGGER.info("Repaired {} demo flight (arrival-before-departure DSL)", designator);
            } else {
                LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            }
            return;
        }

        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                FUEL.toFuelLoad(),
                invalidDsl,
                PLACEHOLDER_JSON);
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
    }

    /**
     * Same pattern as {@code flightplans/invalid_semantic_arrival_before_departure.txt}:
     * arrival time before departure on the same leg.
     */
    private static String dslWithArrivalBeforeDeparture(final String validDsl) {
        final Pattern departure = Pattern.compile(
                "        departure \\w+ (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}) ;");
        final Matcher matcher = departure.matcher(validDsl);
        if (!matcher.find()) {
            return validDsl;
        }
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        final String badArrival = LocalDateTime.parse(matcher.group(1), fmt)
                .minusHours(2)
                .format(fmt);
        return validDsl.replaceFirst(
                "(        arrival \\w+ )\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}( ;)",
                "$1" + badArrival + "$2");
    }

    private static boolean isStaleDslErrorDemo(final String current, final String targetInvalid) {
        if (current == null || current.isBlank() || !current.contains(";")) {
            return true;
        }
        return !current.equals(targetInvalid);
    }

    private static void seedUs100WindowFlights(final RepositoryFactory repos,
                                               final PilotUser pilot,
                                               final Aircraft csDemo,
                                               final Aircraft csTp02) {
        final LocalDate day = nextUs100PassDeparture().toLocalDate();
        final RouteRepository routes = repos.routes();
        final Route opoLis = routes.ofIdentity(RouteName.valueOf("TP1006")).orElse(null);
        final Route lisFao = routes.ofIdentity(RouteName.valueOf("TP1003")).orElse(null);
        final Route faoLis = routes.ofIdentity(RouteName.valueOf("TP1004")).orElse(null);
        final Route lisFnc = routes.ofIdentity(RouteName.valueOf("TP1005")).orElse(null);
        if (opoLis == null || lisFao == null || faoLis == null || lisFnc == null) {
            LOGGER.warn("Skipping US100 window flights — charter routes TP1003–TP1006 missing");
            return;
        }
        final FlightRepository flights = repos.flights();
        final AircraftModelRepository aircraftModels = repos.aircraftModels();
        final EngineModelRepository engineModels = repos.engineModels();
        final int legHours = 1;
        final int legMinutes = 30;

        seedValidFlight(flights, aircraftModels, engineModels, "TP100FULL1", faoLis, pilot, csTp02,
                day.atTime(13, 15), legHours, legMinutes);
        seedValidFlight(flights, aircraftModels, engineModels, "TP100CLIP1", opoLis, pilot, csDemo,
                day.atTime(13, 45), legHours, legMinutes);
        seedValidFlight(flights, aircraftModels, engineModels, "TP100PASS", lisFao, pilot, csTp02,
                day.atTime(15, 0), legHours, legMinutes);
        seedValidFlight(flights, aircraftModels, engineModels, "TP100FULL2", lisFao, pilot, csDemo,
                day.atTime(15, 30), legHours, legMinutes);
        seedValidFlight(flights, aircraftModels, engineModels, "TP100CLIP2", lisFnc, pilot, csTp02,
                day.atTime(16, 45), legHours, legMinutes);
    }

    private static void seedValidFlight(final FlightRepository flights,
                                        final AircraftModelRepository aircraftModels,
                                        final EngineModelRepository engineModels,
                                        final String designator,
                                        final Route route,
                                        final PilotUser pilot,
                                        final Aircraft aircraft,
                                        final LocalDateTime departure,
                                        final int durationHours,
                                        final int durationMinutes) {
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            return;
        }
        final FlightDesignator id = new FlightDesignator(designator);
        final FlightSchedule schedule = new FlightSchedule(
                departure, departure.plusHours(durationHours).plusMinutes(durationMinutes));
        final DirectRouteFlightPlanComposer.ComposedPlan composed = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels);
        final String dsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                FUEL.toFuelLoad(),
                dsl,
                composed.jsonContent());
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
        LOGGER.info("Seeded US100 window flight {} at {}", designator, departure);
    }

    private static void seedClippedDemo(final FlightRepository flights,
                                        final AircraftModelRepository aircraftModels,
                                        final EngineModelRepository engineModels,
                                        final Route route,
                                        final PilotUser pilot,
                                        final Aircraft aircraft,
                                        final FlightSchedule schedule) {
        final String designator = "TP100CLIPPED";
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            return;
        }
        final FlightDesignator id = new FlightDesignator(designator);
        final DirectRouteFlightPlanComposer.ComposedPlan composed = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels);
        final String dsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                FUEL.toFuelLoad(),
                dsl,
                composed.jsonContent());
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
    }

    private static void seedUs085ClientFixture(final FlightRepository flights,
                                               final Route route,
                                               final PilotUser pilot,
                                               final Aircraft aircraft) {
        final String designator = "AA123";
        if (flights.findByDesignator(new FlightDesignator(designator)).isPresent()) {
            LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            return;
        }
        final LocalDateTime departure = nextUs085ClientDeparture();
        final FlightSchedule schedule = new FlightSchedule(departure, departure.plusHours(2));
        final FlightDesignator id = new FlightDesignator(designator);
        final String dsl = us085ClientDsl(departure, schedule.scheduledArrival());
        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                US085_CLIENT_FUEL.toFuelLoad(),
                dsl,
                loadUs085ClientJson());
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
        LOGGER.info("Seeded US085 client fixture {} at {}", designator, departure);
    }

    private static String loadUs085ClientJson() {
        try (InputStream in = FlightBootstrapper.class.getResourceAsStream(US085_CLIENT_JSON_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing bootstrap resource: " + US085_CLIENT_JSON_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load US085 client fixture JSON", ex);
        }
    }

    private static String us085ClientDsl(final LocalDateTime departure, final LocalDateTime arrival) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        return """
                flight AA123 {
                    type REGULAR ;
                    load passengers 0 pax_weight 0.0 kg cargo_weight 42850.0 kg ;

                    leg {
                        departure OPO %s ;
                        arrival MAD %s ;

                        route {
                            segment (41.262891, -8.68522) (41.5, -8.16)
                                alt 7405 m width 500 m ;
                                wind 90 10.5 m/s ;
                            segment (41.5, -8.16) (41.0, -5.4)
                                alt 7405 m width 500 m ;
                                wind 90 10.5 m/s ;
                            segment (41.0, -5.4) (40.4895, -3.5643)
                                alt 7405 m width 500 m ;
                                wind 90 10.5 m/s ;
                        }

                        fuel 39000.0 l ;
                    }
                }
                """.formatted(departure.format(fmt), arrival.format(fmt));
    }

    private static void seedSimulationError(final FlightRepository flights,
                                            final AircraftModelRepository aircraftModels,
                                            final EngineModelRepository engineModels,
                                            final Route route,
                                            final PilotUser pilot,
                                            final Aircraft aircraft,
                                            final FlightSchedule schedule) {
        final String designator = "TP085SIM";
        final FlightDesignator id = new FlightDesignator(designator);
        final DirectRouteFlightPlanComposer.ComposedPlan composed = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels);
        final DirectRouteFlightPlanComposer.ComposedPlan starving = composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels, SIM_FAIL_LEG_FUEL);
        final String dsl = FlightPlanDslExporter.toDsl(composed.descriptor());
        final String failJson = starving.jsonContent();

        final var existing = flights.findByDesignator(id);
        if (existing.isPresent()) {
            final Flight flight = existing.get();
            final String storedJson = flight.flightPlan().jsonContent();
            final boolean staleEmptyLegJson = storedJson != null && storedJson.contains("\"Leg\": []");
            final boolean wronglyApproved = flight.flightPlan().status() == FlightPlanStatus.SIM_APPROVED;
            if (staleEmptyLegJson || wronglyApproved) {
                flight.flightPlan().replaceDraftContent(FUEL.toFuelLoad(), dsl, failJson);
                flights.save(flight);
                LOGGER.info("Repaired {} demo flight (reset to insufficient-fuel simulator JSON)", designator);
            } else {
                LOGGER.debug("Assuming flight {} already exists (skip)", designator);
            }
            return;
        }

        final FlightPlan plan = FlightPlan.forFlight(
                id,
                FlightPlanStatus.DRAFT,
                FUEL.toFuelLoad(),
                dsl,
                failJson);
        final Flight flight = Flight.createDraftForRoute(id, route, aircraft, pilot, schedule, plan);
        flights.save(flight);
    }

    private static DirectRouteFlightPlanComposer.ComposedPlan composeForAircraft(
            final FlightDesignator id,
            final Route route,
            final Aircraft aircraft,
            final FlightSchedule schedule,
            final AircraftModelRepository aircraftModels,
            final EngineModelRepository engineModels) {
        return composeForAircraft(
                id, route, aircraft, schedule, aircraftModels, engineModels, FUEL);
    }

    private static DirectRouteFlightPlanComposer.ComposedPlan composeForAircraft(
            final FlightDesignator id,
            final Route route,
            final Aircraft aircraft,
            final FlightSchedule schedule,
            final AircraftModelRepository aircraftModels,
            final EngineModelRepository engineModels,
            final FuelQuantity legFuel) {
        final AircraftModel model = aircraftModels.ofIdentity(aircraft.aircraftModelId())
                .orElseThrow(() -> new IllegalStateException(
                        "Aircraft model not found: " + aircraft.aircraftModelId()));
        final EngineModel engine = engineModels.findByModelId(aircraft.engineModelId())
                .orElseThrow(() -> new IllegalStateException(
                        "Engine model not found: " + aircraft.engineModelId()));
        return DirectRouteFlightPlanComposer.compose(
                id,
                route,
                route.originAirport(),
                route.destinationAirport(),
                schedule.scheduledDeparture(),
                schedule.scheduledArrival(),
                legFuel,
                PASSENGER_COUNT,
                PASSENGER_WEIGHT_KG,
                CARGO_WEIGHT_KG,
                aircraft.aircraftModelId().toString(),
                model,
                engine);
    }

    private static Aircraft requireAircraft(final AircraftRepository aircraft, final String registration) {
        return aircraft.findByRegistration(AircraftRegistration.valueOf(registration))
                .orElseThrow(() -> new IllegalStateException("Aircraft not found: " + registration));
    }

    /** First Monday or Thursday on or after one month from today, at 10:00 (TP1001 schedule). */
    static LocalDateTime nextTp1001Departure() {
        LocalDate date = LocalDate.now().plusMonths(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY && date.getDayOfWeek() != DayOfWeek.THURSDAY) {
            date = date.plusDays(1);
        }
        return date.atTime(10, 0);
    }

    /** First Tuesday on or after two months from today, at 14:00 — isolated US100 PASS demo (LIS→FAO). */
    static LocalDateTime nextUs100PassDeparture() {
        LocalDate date = LocalDate.now().plusMonths(2);
        while (date.getDayOfWeek() != DayOfWeek.TUESDAY) {
            date = date.plusDays(1);
        }
        return date.atTime(14, 0);
    }

    /** First Monday on or after one month from today, at 09:00 — US085 LAPR4 client fixture (OPO→MAD). */
    static LocalDateTime nextUs085ClientDeparture() {
        LocalDate date = LocalDate.now().plusMonths(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date.atTime(9, 0);
    }
}
