package eapli.aisafe.flightmanagement.application;

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
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.framework.infrastructure.authz.domain.model.Username;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class CreateFlightPlanService {

    private final FlightRepository flights;
    private final RouteRepository routes;
    private final AircraftRepository aircraft;
    private final PilotUserRepository pilots;
    private final AircraftModelRepository aircraftModels;
    private final EngineModelRepository engineModels;

    public CreateFlightPlanService(final FlightRepository flights,
                                   final RouteRepository routes,
                                   final AircraftRepository aircraft,
                                   final PilotUserRepository pilots,
                                   final AircraftModelRepository aircraftModels,
                                   final EngineModelRepository engineModels) {
        if (flights == null || routes == null || aircraft == null || pilots == null
                || aircraftModels == null || engineModels == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.flights = flights;
        this.routes = routes;
        this.aircraft = aircraft;
        this.pilots = pilots;
        this.aircraftModels = aircraftModels;
        this.engineModels = engineModels;
    }

    public List<Route> listSelectableRoutes(final Username sessionPilotUsername, final LocalDate asOf) {
        final PilotUser pilot = requirePilot(sessionPilotUsername);
        final List<Route> result = new ArrayList<>();
        for (final Route route : routes.findActiveByCompany(pilot.airTransportCompany().identity(), asOf)) {
            result.add(route);
        }
        result.sort(Comparator.comparing(r -> r.identity().toString()));
        return result;
    }

    public List<String> listCompanyActiveAircraftRegistrations(final Username sessionPilotUsername) {
        final IATACode company = requirePilot(sessionPilotUsername).airTransportCompany().identity();
        final List<String> registrations = new ArrayList<>();
        for (final Aircraft aircraftEntity : aircraft.findActiveByOwnerCompany(company)) {
            registrations.add(aircraftEntity.identity().toString());
        }
        registrations.sort(Comparator.naturalOrder());
        return registrations;
    }

    public List<PilotUser> listCompanyPilots(final Username sessionPilotUsername) {
        final PilotUser sessionPilot = requirePilot(sessionPilotUsername);
        final List<PilotUser> result = new ArrayList<>();
        for (final PilotUser pilot : pilots.findPilotByCompanyAndActive(sessionPilot.airTransportCompany())) {
            result.add(pilot);
        }
        result.sort(Comparator.comparing(p -> p.systemUser().name().firstName()));
        return result;
    }

    public CreateFlightPlanResult createFlightPlan(final CreateFlightPlanRequest request) {
        try {
            final Route route = routes.ofIdentity(RouteName.valueOf(request.routeName()))
                    .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.routeName()));

            final PilotUser pilot = pilots.findByUsername(Username.valueOf(request.pilotUsername()))
                    .orElseThrow(() -> new IllegalArgumentException("Pilot not found: " + request.pilotUsername()));

            if (!pilot.systemUser().isActive()) {
                return CreateFlightPlanResult.failure("Pilot is not active.");
            }

            final AircraftRegistration registration = AircraftRegistration.valueOf(
                    request.aircraftRegistration().trim());
            final Aircraft aircraftEntity = aircraft.findByRegistration(registration)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown aircraft registration: " + request.aircraftRegistration()));

            final AircraftModel model = aircraftModels.ofIdentity(aircraftEntity.aircraftModelId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown aircraft model: " + aircraftEntity.aircraftModelId()));
            final EngineModel engine = engineModels.findByModelId(aircraftEntity.engineModelId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown engine model: " + aircraftEntity.engineModelId()));

            final FlightDesignator designator = FlightDesignator.fromRoute(
                    route.identity(), request.operationalSuffix());

            final DirectRouteFlightPlanComposer.ComposedPlan composed =
                    DirectRouteFlightPlanComposer.compose(
                            designator,
                            route,
                            route.originAirport(),
                            route.destinationAirport(),
                            request.departure(),
                            request.arrival(),
                            request.fuel(),
                            request.passengerCount(),
                            request.passengerWeightKg(),
                            request.cargoWeightKg(),
                            aircraftEntity.aircraftModelId().toString(),
                            model,
                            engine);

            final String dslContent = FlightPlanDslExporter.toDsl(composed.descriptor());
            final FlightSchedule schedule = new FlightSchedule(request.departure(), request.arrival());
            final FlightPlan plan = FlightPlan.forFlight(
                    designator,
                    FlightPlanStatus.DRAFT,
                    request.fuel().toFuelLoad(),
                    dslContent,
                    composed.jsonContent());

            final Optional<Flight> existing = flights.findByDesignator(designator);
            if (existing.isPresent()) {
                return replaceExistingFlight(existing.get(), route, aircraftEntity, pilot, schedule, plan, request);
            }

            final Flight flight = Flight.createDraftForRoute(
                    designator, route, aircraftEntity, pilot, schedule, plan);
            flights.save(flight);
            return CreateFlightPlanResult.success(designator);
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            return CreateFlightPlanResult.failure(ex.getMessage());
        }
    }

    private CreateFlightPlanResult replaceExistingFlight(final Flight existing,
                                                           final Route route,
                                                           final Aircraft aircraftEntity,
                                                           final PilotUser pilot,
                                                           final FlightSchedule schedule,
                                                           final FlightPlan plan,
                                                           final CreateFlightPlanRequest request) {
        if (existing.flightPlan() == null) {
            return CreateFlightPlanResult.failure(
                    "Flight " + existing.designator() + " has no flight plan to replace.");
        }

        final FlightPlanStatus currentStatus = existing.flightPlan().status();
        if (!currentStatus.allowsSilentReplacement() && !request.confirmReplace()) {
            return CreateFlightPlanResult.needsConfirmation(existing.designator(), currentStatus);
        }

        existing.replaceFlightPlan(route, aircraftEntity, pilot, schedule, plan);
        flights.save(existing);
        return CreateFlightPlanResult.replaced(existing.designator(), currentStatus);
    }

    private PilotUser requirePilot(final Username username) {
        return pilots.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No pilot linked to user"));
    }
}
