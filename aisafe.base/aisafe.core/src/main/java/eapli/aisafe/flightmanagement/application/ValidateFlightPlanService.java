package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector;
import eapli.aisafe.flightmanagement.infrastructure.simulator.Lapr4FlightPlanJson;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorFlightId;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

public final class ValidateFlightPlanService {

    private static final DateTimeFormatter LEG_DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final FlightRepository flights;
    private final RouteRepository routes;
    private final AircraftRepository aircraft;
    private final AirportRepository airports;
    private final AirControlAreaRepository areaRepository;
    private final FlightDslParser dslParser;
    private final FlightSimulationService simulationService;
    private final RouteAreaCrossingDetector crossingDetector;

    public ValidateFlightPlanService(final FlightRepository flights,
                                       final RouteRepository routes,
                                       final AircraftRepository aircraft,
                                       final AirportRepository airports,
                                       final AirControlAreaRepository areaRepository,
                                       final FlightSimulationService simulationService) {
        this(flights, routes, aircraft, airports, areaRepository, new FlightDslParser(), simulationService,
                new RouteAreaCrossingDetector());
    }

    ValidateFlightPlanService(final FlightRepository flights,
                              final RouteRepository routes,
                              final AircraftRepository aircraft,
                              final AirportRepository airports,
                              final AirControlAreaRepository areaRepository,
                              final FlightDslParser dslParser,
                              final FlightSimulationService simulationService,
                              final RouteAreaCrossingDetector crossingDetector) {
        this.flights = Objects.requireNonNull(flights, "flights");
        this.routes = Objects.requireNonNull(routes, "routes");
        this.aircraft = Objects.requireNonNull(aircraft, "aircraft");
        this.airports = Objects.requireNonNull(airports, "airports");
        this.areaRepository = Objects.requireNonNull(areaRepository, "areaRepository");
        this.dslParser = Objects.requireNonNull(dslParser, "dslParser");
        this.simulationService = Objects.requireNonNull(simulationService, "simulationService");
        this.crossingDetector = Objects.requireNonNull(crossingDetector, "crossingDetector");
    }

    public ValidateFlightPlanResult validate(final String flightDesignatorText, final SystemUser authenticatedPilot) {
        return validate(flightDesignatorText, authenticatedPilot, null);
    }

    /**
     * Flights owned by {@code authenticatedPilot} whose plan is still {@code DRAFT}
     * (the only state US085 can validate). Returned newest-departure first.
     */
    public List<FlightValidationPreview> listValidatableForPilot(final SystemUser authenticatedPilot) {
        if (authenticatedPilot == null) {
            return List.of();
        }
        final List<FlightValidationPreview> previews = new ArrayList<>(
                flights.findDraftFlightsForPilot(authenticatedPilot));
        previews.sort(Comparator.comparing(
                FlightValidationPreview::scheduledDeparture,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return previews;
    }

    public ValidateFlightPlanResult validate(final String flightDesignatorText,
                                             final SystemUser authenticatedPilot,
                                             final Runnable onSimulationStart) {
        final String designatorText = flightDesignatorText.trim();
        final Optional<Flight> flightOpt = flights.findByDesignator(new FlightDesignator(designatorText));
        if (flightOpt.isEmpty()) {
            return ValidateFlightPlanResult.blocked(designatorText, "Flight not found: " + flightDesignatorText);
        }
        final Flight flight = flightOpt.get();

        final Optional<String> preflightError = preflightChecks(flight, authenticatedPilot);
        if (preflightError.isPresent()) {
            return ValidateFlightPlanResult.blocked(designatorText, preflightError.get());
        }

        final FlightPlan plan = flight.flightPlan();
        final ParseResult parsed = dslParser.parse(plan.dslContent());
        if (!parsed.isValid()) {
            return ValidateFlightPlanResult.dslFailure(designatorText, parsed.getErrors(), plan.dslContent());
        }

        final FlightPlanDescriptor descriptor = parsed.getDescriptor();
        final Optional<String> consistencyError = validateDslConsistency(flight, descriptor);
        if (consistencyError.isPresent()) {
            return ValidateFlightPlanResult.blocked(designatorText, consistencyError.get());
        }

        final Optional<String> businessError = validateBusinessRules(flight);
        if (businessError.isPresent()) {
            return ValidateFlightPlanResult.blocked(designatorText, businessError.get());
        }

        final Optional<LocalDateTime> intervalStart = intervalStart(descriptor, flight);
        if (intervalStart.isEmpty()) {
            return ValidateFlightPlanResult.blocked(designatorText, "Cannot derive simulation interval start.");
        }
        final Optional<LocalDateTime> intervalEnd = intervalEnd(descriptor, flight);
        if (intervalEnd.isEmpty()) {
            return ValidateFlightPlanResult.blocked(designatorText, "Cannot derive simulation interval end.");
        }

        final Set<String> crossedAreas = resolveAreasCrossed(descriptor);
        if (crossedAreas.isEmpty()) {
            return ValidateFlightPlanResult.blocked(designatorText, "Could not derive air control area for simulation.");
        }
        final String areaCode = crossedAreas.iterator().next();

        if (eligibleTargetFlight(flight, descriptor, intervalStart.get(), intervalEnd.get()).isEmpty()) {
            return ValidateFlightPlanResult.blocked(
                    designatorText,
                    "Target flight is not eligible for simulation in area " + areaCode + ".");
        }

        return runSimulation(
                flight, designatorText, areaCode, intervalStart.get(), intervalEnd.get(),
                List.of(flight), onSimulationStart);
    }

    private ValidateFlightPlanResult runSimulation(final Flight flight,
                                                   final String designatorText,
                                                   final String areaCode,
                                                   final LocalDateTime intervalStart,
                                                   final LocalDateTime intervalEnd,
                                                   final List<Flight> eligible,
                                                   final Runnable onSimulationStart) {
        flight.transitionFlightPlanStatus(FlightPlanStatus.SUBMITTED_FOR_SIMULATION);
        try {
            if (onSimulationStart != null) {
                onSimulationStart.run();
            }
            final SimulationResult simulation = simulationService.simulateEligibleFlights(
                    areaCode, intervalStart, intervalEnd, eligible, designatorText);
            final List<String> simulationLog = simulation.simulationLog();
            if (simulation.passed() && targetFlightSucceeded(flight, simulation)) {
                flight.transitionFlightPlanStatus(FlightPlanStatus.SIM_APPROVED);
                flights.save(flight);
                return ValidateFlightPlanResult.approved(designatorText, simulationLog);
            }
            flight.transitionFlightPlanStatus(FlightPlanStatus.SIM_REJECTED);
            flights.save(flight);
            return ValidateFlightPlanResult.rejected(
                    designatorText, simulationFailureReason(flight, simulation), simulationLog);
        } catch (final SimulatorExecutionException ex) {
            flight.transitionFlightPlanStatus(FlightPlanStatus.SIM_REJECTED);
            flights.save(flight);
            return ValidateFlightPlanResult.rejected(
                    designatorText, ex.getMessage(), ex.simulatorOutput());
        }
    }

    private static boolean targetFlightSucceeded(final Flight flight, final SimulationResult simulation) {
        return targetFlightStatus(flight, simulation)
                .map(status -> status == FlightExecutionStatus.SUCCESS)
                .orElse(false);
    }

    private static Optional<FlightExecutionStatus> targetFlightStatus(final Flight flight,
                                                                      final SimulationResult simulation) {
        final String targetId = resolveTargetFlightReportId(flight);
        return simulation.report().flightStatuses().stream()
                .filter(status -> status.flightId().equals(targetId))
                .map(FlightStatus::executionStatus)
                .findFirst();
    }

    private static String resolveTargetFlightReportId(final Flight flight) {
        return Lapr4FlightPlanJson.extractNumericId(flight.flightPlan().jsonContent())
                .map(String::valueOf)
                .orElseGet(() -> String.valueOf(simulatorFlightId(flight.identity().toString())));
    }

    private static String simulationFailureReason(final Flight flight, final SimulationResult simulation) {
        if (!simulation.passed()) {
            return simulation.failureReason();
        }
        return targetFlightStatus(flight, simulation)
                .map(FlightExecutionStatus::userMessage)
                .orElse("Target flight not found in simulation report.");
    }

    private static int simulatorFlightId(final String flightId) {
        return SimulatorFlightId.fromDesignator(flightId);
    }

    private static Optional<String> preflightChecks(final Flight flight, final SystemUser authenticatedPilot) {
        if (flight.flightPlan() == null || flight.flightPlan().status() != FlightPlanStatus.DRAFT) {
            return Optional.of("Only a DRAFT flight plan can be validated.");
        }
        if (flight.pilot() == null || flight.pilot().systemUser() == null) {
            return Optional.of("Flight has no pilot assigned.");
        }
        if (!flight.pilot().systemUser().equals(authenticatedPilot)) {
            return Optional.of("Only the owner pilot may validate this flight plan.");
        }
        final FlightPlan plan = flight.flightPlan();
        if (!plan.hasDslContent()) {
            return Optional.of("Flight plan has no stored DSL to validate.");
        }
        if (!plan.hasJsonContent()) {
            return Optional.of("Flight plan has no simulator JSON content.");
        }
        return Optional.empty();
    }

    private Optional<Flight> eligibleTargetFlight(final Flight target,
                                                  final FlightPlanDescriptor descriptor,
                                                  final LocalDateTime intervalStart,
                                                  final LocalDateTime intervalEnd) {
        if (!target.hasPlanReadyForSimulation() || !target.scheduleOverlaps(intervalStart, intervalEnd)) {
            return Optional.empty();
        }
        if (resolveAreasCrossed(descriptor).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(target);
    }

    private Set<String> resolveAreasCrossed(final FlightPlanDescriptor descriptor) {
        final Set<String> crossed = new LinkedHashSet<>();
        for (final AirControlArea area : StreamSupport.stream(areaRepository.findAll().spliterator(), false).toList()) {
            final GeographicBoundary boundary = area.getGeographicBoundary();
            if (crossingDetector.crossesArea(descriptor, boundary)) {
                crossed.add(area.identity().toString());
            }
        }
        return crossed;
    }

    private static Optional<LocalDateTime> intervalStart(final FlightPlanDescriptor descriptor, final Flight flight) {
        if (!descriptor.getLegs().isEmpty()) {
            return Optional.of(LocalDateTime.parse(descriptor.getLegs().get(0).getDepartureTime(), LEG_DATE_TIME));
        }
        final FlightSchedule schedule = flight.schedule();
        return schedule != null ? Optional.of(schedule.scheduledDeparture()) : Optional.empty();
    }

    private static Optional<LocalDateTime> intervalEnd(final FlightPlanDescriptor descriptor, final Flight flight) {
        if (!descriptor.getLegs().isEmpty()) {
            final List<LegDescriptor> legs = descriptor.getLegs();
            return Optional.of(LocalDateTime.parse(legs.get(legs.size() - 1).getArrivalTime(), LEG_DATE_TIME));
        }
        final FlightSchedule schedule = flight.schedule();
        return schedule != null ? Optional.of(schedule.scheduledArrival()) : Optional.empty();
    }

    private Optional<String> validateDslConsistency(final Flight flight, final FlightPlanDescriptor descriptor) {
        if (!flight.identity().toString().equalsIgnoreCase(descriptor.getFlightId())) {
            return Optional.of(String.format(
                    "DSL flight id '%s' does not match flight '%s'.",
                    descriptor.getFlightId(), flight.identity()));
        }

        if (descriptor.getLegs().isEmpty()) {
            return Optional.empty();
        }

        final LegDescriptor first = descriptor.getLegs().get(0);
        final LegDescriptor last = descriptor.getLegs().get(descriptor.getLegs().size() - 1);
        final LocalDateTime dslDeparture = LocalDateTime.parse(first.getDepartureTime(), LEG_DATE_TIME);

        final Optional<Route> routeOpt = routes.ofIdentity(RouteName.valueOf(flight.routeName()));
        if (routeOpt.isPresent()) {
            final Route route = routeOpt.get();
            if (!route.originAirportIATACode().toString().equalsIgnoreCase(first.getDepartureAirport())) {
                return Optional.of(String.format(
                        "DSL departure airport '%s' does not match route origin '%s'.",
                        first.getDepartureAirport(), route.originAirportIATACode()));
            }
            if (!route.destinationAirportIATACode().toString().equalsIgnoreCase(last.getArrivalAirport())) {
                return Optional.of(String.format(
                        "DSL arrival airport '%s' does not match route destination '%s'.",
                        last.getArrivalAirport(), route.destinationAirportIATACode()));
            }
        }

        if (flight.schedule() != null
                && !flight.schedule().scheduledDeparture().equals(dslDeparture)) {
            return Optional.of(String.format(
                    "DSL departure '%s' does not match flight schedule '%s'.",
                    dslDeparture, flight.schedule().scheduledDeparture()));
        }

        for (int i = 0; i < descriptor.getLegs().size() - 1; i++) {
            final String arrivalAirport = descriptor.getLegs().get(i).getArrivalAirport();
            final String nextDeparture = descriptor.getLegs().get(i + 1).getDepartureAirport();
            if (!arrivalAirport.equalsIgnoreCase(nextDeparture)) {
                return Optional.of(String.format(
                        "Multi-leg inconsistency between leg %d and %d.", i + 1, i + 2));
            }
        }
        return Optional.empty();
    }

    private Optional<String> validateBusinessRules(final Flight flight) {
        if (flight.schedule() == null) {
            return Optional.of("Flight has no schedule.");
        }
        final LocalDateTime departure = flight.schedule().scheduledDeparture();

        final Optional<Route> routeOpt = routes.ofIdentity(RouteName.valueOf(flight.routeName()));
        if (routeOpt.isPresent() && !routeOpt.get().isActiveOn(departure.toLocalDate())) {
            return Optional.of("Selected flight route is inactive.");
        }

        final AircraftRegistration registration = AircraftRegistration.valueOf(flight.aircraftRegistration());
        final Optional<Aircraft> aircraftOpt = aircraft.findByRegistration(registration);
        if (aircraftOpt.isEmpty()) {
            return Optional.of("Associated aircraft not found.");
        }
        final Aircraft aircraftEntity = aircraftOpt.get();
        if (!aircraftEntity.isActive()) {
            return Optional.of("Cannot assign a decommissioned aircraft to a flight.");
        }

        final PilotUser pilot = flight.pilot();
        if (pilot == null) {
            return Optional.of("Flight has no assigned pilot.");
        }
        if (routeOpt.isPresent()) {
            final Route route = routeOpt.get();
            if (!pilot.airTransportCompany().identity().equals(route.companyIATACode())) {
                return Optional.of("Pilot does not belong to the route's company.");
            }
            if (!aircraftEntity.ownerCompanyIata().equals(route.companyIATACode())) {
                return Optional.of("Aircraft does not belong to the route's company.");
            }
        }
        if (!pilot.isCertifiedFor(aircraftEntity.aircraftModelId())) {
            return Optional.of("Pilot is not certified for the selected aircraft model.");
        }
        return Optional.empty();
    }
}
