package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.aircraftmanagement.application.DecommissionAircraftController;
import eapli.aisafe.aircraftmanagement.application.FleetListCriteria;
import eapli.aisafe.aircraftmanagement.application.ListFleetController;
import eapli.aisafe.aircraftmanagement.application.RegisterAircraftController;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.companycollaboratormanagment.application.AddPilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.DeactivatePilotResult;
import eapli.aisafe.companycollaboratormanagment.application.ListPilotUsersController;
import eapli.aisafe.companycollaboratormanagment.application.RemovePilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;
import eapli.aisafe.companycollaboratormanagment.dto.CreatePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.routemanagement.application.CreateRouteController;
import eapli.aisafe.routemanagement.application.DeactivateRouteController;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AtccCommandHandler {
    private final ClientHandler session;
    private final RegisterAircraftController registerAircraft;
    private final DecommissionAircraftController decommissionAircraft;
    private final ListFleetController listFleet;
    private final ListAircraftModelsController listAircraftModels;
    private final AddPilotCollaboratorController addPilot;
    private final ListPilotUsersController listPilots;
    private final RemovePilotCollaboratorController removePilot;
    private final CreateRouteController createRoute;
    private final DeactivateRouteController deactivateRoute;

    public AtccCommandHandler(ClientHandler session) {
        this(session,
                new RegisterAircraftController(),
                new DecommissionAircraftController(),
                new ListFleetController(),
                new ListAircraftModelsController(),
                new AddPilotCollaboratorController(),
                new ListPilotUsersController(),
                new RemovePilotCollaboratorController(),
                new CreateRouteController(),
                new DeactivateRouteController());
    }

    AtccCommandHandler(final ClientHandler session,
                       final RegisterAircraftController registerAircraft,
                       final DecommissionAircraftController decommissionAircraft,
                       final ListFleetController listFleet,
                       final ListAircraftModelsController listAircraftModels,
                       final AddPilotCollaboratorController addPilot,
                       final ListPilotUsersController listPilots,
                       final RemovePilotCollaboratorController removePilot,
                       final CreateRouteController createRoute,
                       final DeactivateRouteController deactivateRoute) {
        this.session = session;
        this.registerAircraft = registerAircraft;
        this.decommissionAircraft = decommissionAircraft;
        this.listFleet = listFleet;
        this.listAircraftModels = listAircraftModels;
        this.addPilot = addPilot;
        this.listPilots = listPilots;
        this.removePilot = removePilot;
        this.createRoute = createRoute;
        this.deactivateRoute = deactivateRoute;
    }

    public void handle(final byte opcode, final String payload, final DataOutputStream out) throws IOException {
        if (AuthzRegistry.authorizationService().session().isEmpty()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.UNAUTHORIZED, "Login required.");
            return;
        }
        if (!AuthzRegistry.authorizationService()
                .isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "ATCC role required.");
            return;
        }

        try {
            switch (opcode) {
                case AtccOpcodes.LIST_AIRCRAFT_MODELS -> listAircraftModels(payload, out);
                case AtccOpcodes.REGISTER_AIRCRAFT -> registerAircraft(payload, out);
                case AtccOpcodes.DECOMMISSION_AIRCRAFT -> decommission(payload, out);
                case AtccOpcodes.LIST_ACTIVE_AIRCRAFT -> listActive(payload, out);
                case AtccOpcodes.LIST_FLEET -> listFleetFiltered(payload, out);
                case AtccOpcodes.LIST_FLEET_MODELS -> listFleetModels(out);
                case AtccOpcodes.LIST_FLEET_MANUFACTURERS -> listFleetManufacturers(out);
                case AtccOpcodes.CREATE_ROUTE -> createRoute(payload, out);
                case AtccOpcodes.ROUTE_COMPANY_CONTEXT -> routeCompanyContext(out);
                case AtccOpcodes.VALIDATE_ROUTE_NAME -> validateRouteName(payload, out);
                case AtccOpcodes.LIST_ROUTE_AIRPORTS -> listRouteAirports(out);
                case AtccOpcodes.LIST_ACTIVE_ROUTES -> listActiveRoutes(out);
                case AtccOpcodes.DEACTIVATE_ROUTE -> deactivateRoute(payload, out);
                case AtccOpcodes.REMOVE_PILOT -> removePilot(payload, out);
                case AtccOpcodes.ADD_PILOT_NEW_USER -> addPilotNew(payload, out);
                case AtccOpcodes.LIST_PILOT_ROSTER -> listPilotRoster(out);
                case AtccOpcodes.LIST_AIRCRAFT_MODEL_IDS -> listModelIds(out);
                case AtccOpcodes.LIST_CERTIFIED_ENGINES -> listCertifiedEngines(payload, out);
                default -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, "Unknown ATCC opcode: " + opcode);
            }
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        } catch (final RuntimeException ex) {
            System.err.println("[ATCC] Unexpected error handling opcode " + opcode + ": " + ex);
            ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR,
                    "An unexpected error occurred. Please check the server logs.");
        }
    }

    private void notImplemented(final DataOutputStream out, final String feature) throws IOException {
        ProtocolFrame.writeResponse(out, ResponseCodes.NOT_IMPLEMENTED,
                feature + " is not available yet on the server.");
    }

    private void listAircraftModels(final String payload, final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final AircraftModel model : listAircraftModels.allAircraftModels()) {
            lines.add(AtccResponseFormatter.formatAircraftModel(model));
        }
        session.logAction(null, "LIST_AIRCRAFT_MODELS");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void registerAircraft(final String payload, final DataOutputStream out) throws IOException {
        final String[] f = AtccPayloadParser.splitFields(AtccPayloadParser.stripCertTokens(payload));
        if (f.length < 9) {
            throw new IllegalArgumentException(
                    "Expected: registration;modelId;engineModelId;economy;business;first;country;crew;year");
        }
        final Aircraft aircraft = registerAircraft.registerAircraft(
                f[0].trim(), f[1].trim(), f[2].trim(),
                Integer.parseInt(f[3].trim()), Integer.parseInt(f[4].trim()), Integer.parseInt(f[5].trim()),
                f[6].trim(), Integer.parseInt(f[7].trim()), Integer.parseInt(f[8].trim()));
        session.logAction(null, "REGISTER_AIRCRAFT");
        ok(out, "OK|" + AtccResponseFormatter.formatAircraft(aircraft));
    }

    private void decommission(final String payload, final DataOutputStream out) throws IOException {
        final Aircraft aircraft = decommissionAircraft.decommissionAircraft(
                AtccPayloadParser.registrationOnly(payload));
        session.logAction(null, "DECOMMISSION_AIRCRAFT");
        ok(out, "OK|" + AtccResponseFormatter.formatAircraft(aircraft));
    }

    private void listActive(final String payload, final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final Aircraft aircraft : decommissionAircraft.listActiveCompanyAircraft()) {
            lines.add(AtccResponseFormatter.formatAircraft(aircraft));
        }
        session.logAction(null, "LIST_ACTIVE_AIRCRAFT");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void listFleetFiltered(final String payload, final DataOutputStream out) throws IOException {
        final FleetListCriteria criteria = FleetCriteriaParser.parse(payload);
        final List<String> lines = new ArrayList<>();
        for (final Aircraft aircraft : listFleet.listFleet(criteria)) {
            lines.add(AtccResponseFormatter.formatAircraft(aircraft));
        }
        session.logAction(null, "LIST_FLEET");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void listFleetModels(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        listFleet.modelsUsedInCompanyFleet().forEach(m -> lines.add(AtccResponseFormatter.formatModelId(m.identity())));
        session.logAction(null, "LIST_FLEET_MODELS");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void listFleetManufacturers(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        listFleet.manufacturersUsedInCompanyFleet()
                .forEach(m -> lines.add(AtccResponseFormatter.formatManufacturerId(m)));
        session.logAction(null, "LIST_FLEET_MANUFACTURERS");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void addPilotNew(final String payload, final DataOutputStream out) throws IOException {
        final List<PilotCertificationSpec> certs = AtccPayloadParser.parseCertifications(payload);
        final String[] f = AtccPayloadParser.splitFields(AtccPayloadParser.stripCertTokens(payload));
        if (f.length < 8) {
            throw new IllegalArgumentException(
                    "Expected: username;password;firstName;lastName;email;securityDate;skillsDate;phone;CERT:...");
        }
        CreatePilotCollaboratorDTO dto = new CreatePilotCollaboratorDTO(
                f[0].trim(), f[1].trim(), f[2].trim(), f[3].trim(), f[4].trim(), f[5].trim(), f[6].trim(), f[7].trim(), certs
        );
        addPilot.addUser(dto);
        session.logAction(null, "ADD_PILOT_NEW_USER");
        ok(out, "Pilot registered.");
    }

    private void removePilot(final String payload, final DataOutputStream out) throws IOException {
        final String email = payload == null ? "" : payload.trim();
        if (email.isEmpty()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, "Expected: pilot email");
            return;
        }
        final DeactivatePilotResult result = removePilot.deactivatePilot(email);
        session.logAction(null, "REMOVE_PILOT");
        switch (result.outcome()) {
            case SUCCESS -> ok(out, "Pilot deactivated.");
            case NOT_FOUND -> ProtocolFrame.writeResponse(out, ResponseCodes.NOT_FOUND, "Pilot not found.");
            case NOT_IN_ROSTER -> ProtocolFrame.writeResponse(out, ResponseCodes.CONFLICT, "Pilot does not belong to your company.");
            case ALREADY_INACTIVE -> ProtocolFrame.writeResponse(out, ResponseCodes.CONFLICT, "Pilot is already inactive.");
            case HAS_ACTIVE_FLIGHTS -> ProtocolFrame.writeResponse(out, ResponseCodes.CONFLICT, "Pilot has active flight plans.");
        }
    }

    private void routeCompanyContext(final DataOutputStream out) throws IOException {
        session.logAction(null, "ROUTE_COMPANY_CONTEXT");
        ok(out, createRoute.currentCompanyContext());
    }

    private void validateRouteName(final String payload, final DataOutputStream out) throws IOException {
        final String suffix = payload == null ? "" : payload.trim();
        if (suffix.isEmpty()) {
            throw new IllegalArgumentException("Route numeric suffix is required.");
        }
        if (!suffix.matches("\\d{1,4}")) {
            throw new IllegalArgumentException("Route numeric suffix must be 1 to 4 digits.");
        }
        final String company = createRoute.currentCompanyContext();
        session.logAction(null, "VALIDATE_ROUTE_NAME");
        ok(out, createRoute.validateRouteName(suffix, company));
    }

    private void listRouteAirports(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final Airport airport : createRoute.listAirports()) {
            lines.add(AtccResponseFormatter.formatAirport(airport));
        }
        session.logAction(null, "LIST_ROUTE_AIRPORTS");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void listActiveRoutes(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final Route route : deactivateRoute.listActiveRoutes()) {
            lines.add(AtccResponseFormatter.formatRoute(route));
        }
        session.logAction(null, "LIST_ACTIVE_ROUTES");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }

    private void createRoute(final String payload, final DataOutputStream out) throws IOException {
        final String[] fields = AtccPayloadParser.splitFields(payload);
        if (fields.length < 4) {
            throw new IllegalArgumentException("Expected: routeName;originAirportIata;destinationAirportIata;flightType;...");
        }
        final String routeName = fields[0].trim();
        final String origin = fields[1].trim();
        final String destination = fields[2].trim();
        final String flightType = fields[3].trim().toUpperCase();

        final Route route;
        if ("CHARTER".equals(flightType)) {
            if (fields.length < 6) {
                throw new IllegalArgumentException("CHARTER expects: ...;CHARTER;scheduledDeparture;scheduledArrival");
            }
            route = createRoute.createCharterRoute(routeName, origin, destination, LocalDate.parse(fields[4].trim()), LocalDate.parse(fields[5].trim()));
        } else if ("REGULAR".equals(flightType)) {
            if (fields.length < 5) {
                throw new IllegalArgumentException("REGULAR expects: ...;REGULAR;MONDAY,TUESDAY,...");
            }
            final List<DayOfWeek> days = parseRecurringDays(fields[4]);
            route = createRoute.createRegularRoute(routeName, origin, destination, days);
        } else {
            throw new IllegalArgumentException("Flight type must be REGULAR or CHARTER.");
        }
        session.logAction(null, "CREATE_ROUTE");
        ok(out, "OK|" + AtccResponseFormatter.formatRoute(route));
    }

    private void deactivateRoute(final String payload, final DataOutputStream out) throws IOException {
        final String[] fields = AtccPayloadParser.splitFields(payload);
        if (fields.length < 2) {
            throw new IllegalArgumentException("Expected: routeName;deactivationDate (ISO date uuuu-MM-dd)");
        }
        final Route route = deactivateRoute.deactivateRoute(
                fields[0].trim(),
                LocalDate.parse(fields[1].trim()));
        session.logAction(null, "DEACTIVATE_ROUTE");
        ok(out, "OK|" + AtccResponseFormatter.formatRoute(route));
    }

    private List<DayOfWeek> parseRecurringDays(final String daysToken) {
        final String[] raw = daysToken.split(",");
        final List<DayOfWeek> days = new ArrayList<>();
        for (final String day : raw) {
            final String normalized = day.trim().toUpperCase();
            if (normalized.isBlank()) continue;
            final DayOfWeek parsed = DayOfWeek.valueOf(normalized);
            if (!days.contains(parsed)) days.add(parsed);
        }
        if (days.isEmpty()) throw new IllegalArgumentException("At least one recurring day is required for REGULAR routes.");
        return days;
    }

    private void listPilotRoster(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final ResponsePilotCollaboratorDTO pilot : listPilots.activePilotUsersForCompany().pilots()) {
            lines.add(AtccResponseFormatter.formatPilot(pilot));
        }
        session.logAction(null, "LIST_PILOT_ROSTER");
        ok(out, AtccResponseFormatter.joinLines(lines));
    }
    
    private void listModelIds(final DataOutputStream out) throws IOException {
        session.logAction(null, "LIST_AIRCRAFT_MODEL_IDS");
        ok(out, String.join("\n", addPilot.getAircraftModelIds()));
    }

    private void listCertifiedEngines(final String payload, final DataOutputStream out) throws IOException {
        final String modelId = payload == null ? "" : payload.trim();
        if (modelId.isEmpty()) {
            throw new IllegalArgumentException("Expected: aircraft model id");
        }
        AircraftModel model = null;
        for (final AircraftModel m : listAircraftModels.allAircraftModels()) {
            if (m.identity().toString().equals(modelId)) {
                model = m;
                break;
            }
        }
        if (model == null) {
            throw new IllegalArgumentException("Aircraft model not found: " + modelId);
        }
        final Set<String> certified = new LinkedHashSet<>();
        model.engineCertifiedConfigurations().stream()
                .filter(cfg -> cfg != null && cfg.engineModel() != null)
                .forEach(cfg -> certified.add(cfg.engineModel().identity().toString()));
        session.logAction(null, "LIST_CERTIFIED_ENGINES");
        ok(out, String.join("\n", certified));
    }

    private void ok(final DataOutputStream out, final String body) throws IOException {
        ProtocolFrame.writeResponse(out, ResponseCodes.OK, body);
    }
}
