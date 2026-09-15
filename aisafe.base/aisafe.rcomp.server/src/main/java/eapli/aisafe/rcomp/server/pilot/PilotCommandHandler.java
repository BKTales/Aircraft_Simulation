package eapli.aisafe.rcomp.server.pilot;

import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanController;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanRequest;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanResult;
import eapli.aisafe.flightmanagement.application.ImportFlightPlanFromFileController;
import eapli.aisafe.flightmanagement.application.ImportFlightPlanResult;
import eapli.aisafe.flightmanagement.application.InsertWeatherInFlightController;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanController;
import eapli.aisafe.flightmanagement.application.ValidateFlightPlanResult;
import eapli.aisafe.flightmanagement.application.importfile.FlightPlanFileExtensions;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.OperationalSuffix;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.rcomp.protocol.PilotCreateFlightPlanPayload;
import eapli.aisafe.rcomp.protocol.PilotFlightPlanPayload;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.ValidateFlightPlanDslFailurePayload;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TCP command handler for Pilot remote access (US086): US080–085, US081/US121.
 */
public final class PilotCommandHandler {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter LOCAL_DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ClientHandler session;
    private final CreateFlightPlanController createFlightPlan;
    private final InsertWeatherInFlightController insertWeather;
    private final ImportFlightPlanFromFileController importFlightPlan;
    private final ValidateFlightPlanController validateFlightPlan;
    private final FlightRepository flightRepository;

    public PilotCommandHandler(final ClientHandler session) {
        this(session,
                new CreateFlightPlanController(),
                new InsertWeatherInFlightController(),
                new ImportFlightPlanFromFileController(),
                new ValidateFlightPlanController(),
                PersistenceContext.repositories().flights());
    }

    PilotCommandHandler(final ClientHandler session,
                        final CreateFlightPlanController createFlightPlan,
                        final InsertWeatherInFlightController insertWeather,
                        final ImportFlightPlanFromFileController importFlightPlan,
                        final ValidateFlightPlanController validateFlightPlan,
                        final FlightRepository flightRepository) {
        this.session = session;
        this.createFlightPlan = createFlightPlan;
        this.insertWeather = insertWeather;
        this.importFlightPlan = importFlightPlan;
        this.validateFlightPlan = validateFlightPlan;
        this.flightRepository = flightRepository;
    }

    public void handle(final byte opcode, final String payload, final DataOutputStream out) throws IOException {
        if (AuthzRegistry.authorizationService().session().isEmpty()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.UNAUTHORIZED, "Login required.");
            return;
        }
        if (!AuthzRegistry.authorizationService().isAuthenticatedUserAuthorizedTo(AISafeRoles.PILOT)) {
            ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "Pilot role required.");
            return;
        }

        try {
            switch (opcode) {
                case PilotOpcodes.PARSE_FLIGHT_PLAN_FILE -> parseFlightPlanFile(payload, out);
                case PilotOpcodes.IMPORT_FLIGHT_PLAN -> importFlightPlan(payload, out);
                case PilotOpcodes.LIST_IMPORT_AIRCRAFT -> listImportAircraft(out);
                case PilotOpcodes.LIST_MY_FLIGHTS -> listMyFlights(out);
                case PilotOpcodes.CREATE_FLIGHT_PLAN -> createFlightPlan(payload, out);
                case PilotOpcodes.LIST_CREATE_ROUTES -> listCreateRoutes(payload, out);
                case PilotOpcodes.LIST_COMPANY_PILOTS -> listCompanyPilots(out);
                case PilotOpcodes.ATTACH_WEATHER -> attachWeather(payload, out);
                case PilotOpcodes.VALIDATE_FLIGHT_PLAN -> validateFlightPlan(payload, out);
                default -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, "Unknown Pilot opcode: " + opcode);
            }
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, ex.getMessage());
        } catch (final Exception ex) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        }
    }

    private void createFlightPlan(final String payload, final DataOutputStream out) throws IOException {
        final PilotCreateFlightPlanPayload.Fields fields = PilotCreateFlightPlanPayload.decode(payload);
        final CreateFlightPlanRequest request = toCreateRequest(fields);
        final CreateFlightPlanResult result = createFlightPlan.createFlightPlan(request);
        if (result.needsConfirmation()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.NEEDS_CONFIRMATION, result.errorMessage());
            return;
        }
        if (!result.isSuccess()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, result.errorMessage());
            return;
        }
        session.logAction(null, "CREATE_FLIGHT_PLAN");
        final String action = result.replaced() ? "REPLACED" : "CREATED";
        final StringBuilder body = new StringBuilder("OK|")
                .append(result.designator().orElseThrow())
                .append("|DRAFT|")
                .append(action);
        if (result.replaced()) {
            body.append('|').append(result.replacedFromStatus().map(Enum::name).orElse(""));
        }
        ok(out, body.toString());
    }

    private void listCreateRoutes(final String payload, final DataOutputStream out) throws IOException {
        final LocalDate asOf = parseLocalDate(payload);
        final List<String> lines = new ArrayList<>();
        for (final var route : createFlightPlan.listSelectableRoutes(asOf)) {
            lines.add(PilotResponseFormatter.formatRoute(route));
        }
        session.logAction(null, "LIST_CREATE_ROUTES");
        ok(out, PilotResponseFormatter.joinLines(lines));
    }

    private void listCompanyPilots(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final var pilot : createFlightPlan.listCompanyPilots()) {
            lines.add(PilotResponseFormatter.formatPilot(pilot));
        }
        session.logAction(null, "LIST_COMPANY_PILOTS");
        ok(out, PilotResponseFormatter.joinLines(lines));
    }

    private static CreateFlightPlanRequest toCreateRequest(final PilotCreateFlightPlanPayload.Fields fields) {
        final Optional<OperationalSuffix> suffix = fields.suffix().isBlank()
                ? Optional.empty()
                : OperationalSuffix.optionalOf(fields.suffix());
        return new CreateFlightPlanRequest(
                fields.routeName(),
                fields.aircraftRegistration(),
                fields.pilotUsername(),
                LocalDateTime.parse(fields.departure(), DATE_TIME),
                LocalDateTime.parse(fields.arrival(), DATE_TIME),
                new FuelQuantity(fields.fuelValue(), fields.fuelUnit()),
                fields.passengerCount(),
                fields.passengerWeightKg(),
                fields.cargoWeightKg(),
                suffix,
                fields.confirmReplace());
    }

    private static LocalDate parseLocalDate(final String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Expected departure date: yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(payload.trim(), LOCAL_DATE);
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date. Use yyyy-MM-dd");
        }
    }

    private void parseFlightPlanFile(final String payload, final DataOutputStream out) throws IOException {
        final PilotFlightPlanPayload.FileContent file = PilotFlightPlanPayload.decodeFile(payload);
        try (TempFlightPlanFile temp = TempFlightPlanFile.write(file.fileName(), file.bytes())) {
            final ParseResult result = importFlightPlan.parseFile(temp.path());
            if (!result.isValid()) {
                ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                        String.join("\n", result.getErrors()));
                return;
            }
            final var d = result.getDescriptor();
            ok(out, "VALID|" + d.getFlightId() + "|" + d.getFlightType() + "|" + d.getLegs().size());
        }
    }

    private void importFlightPlan(final String payload, final DataOutputStream out) throws IOException {
        final PilotFlightPlanPayload.FileContent file = PilotFlightPlanPayload.decodeImport(payload);
        final String aircraftReg = PilotFlightPlanPayload.aircraftRegistration(payload);
        try (TempFlightPlanFile temp = TempFlightPlanFile.write(file.fileName(), file.bytes())) {
            final ImportFlightPlanResult result = importFlightPlan.importValidPlan(temp.path(), aircraftReg);
            if (!result.isSuccess()) {
                ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, result.message());
                return;
            }
            session.logAction(null, "IMPORT_FLIGHT_PLAN");
            ok(out, result.message());
        }
    }

    private void listImportAircraft(final DataOutputStream out) throws IOException {
        ok(out, String.join("\n", importFlightPlan.activeAircraftRegistrations()));
    }

    private void attachWeather(final String payload, final DataOutputStream out) throws IOException {
        final String[] parts = payload.split(";", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected: flightDesignator;weatherDataId");
        }
        final String designator = parts[0].trim();
        final long weatherId = Long.parseLong(parts[1].trim());
        final Flight flight = insertWeather.attachWeatherToFlight(designator, weatherId);
        session.logAction(null, "ATTACH_WEATHER");
        ok(out, "OK|" + PilotResponseFormatter.formatFlight(flight));
    }

    private void validateFlightPlan(final String payload, final DataOutputStream out) throws IOException {
        final String designator = payload == null ? "" : payload.trim();
        if (designator.isBlank()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, "Flight designator is required.");
            return;
        }
        final ValidateFlightPlanResult result = validateFlightPlan.validateFlightPlan(designator);
        if (result.dslFailure()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    ValidateFlightPlanDslFailurePayload.encode(result.dslContent(), result.errors()));
            return;
        }
        session.logAction(null, "VALIDATE_FLIGHT_PLAN");
        if (result.passed()) {
            ok(out, "OK|PASS|" + result.flightDesignator());
            return;
        }
        ok(out, "OK|FAIL|" + result.flightDesignator() + "|" + result.message());
    }

    private void listMyFlights(final DataOutputStream out) throws IOException {
        final String username = AuthzRegistry.authorizationService().session()
                .orElseThrow()
                .authenticatedUser()
                .username()
                .toString();
        final List<String> lines = new ArrayList<>();
        for (final Flight flight : flightRepository.findAll()) {
            if (flight.pilot() != null
                    && flight.pilot().systemUser() != null
                    && username.equals(flight.pilot().systemUser().username().toString())) {
                lines.add(PilotResponseFormatter.formatFlight(flight));
            }
        }
        session.logAction(null, "LIST_MY_FLIGHTS");
        ok(out, PilotResponseFormatter.joinLines(lines));
    }

    private void notImplemented(final DataOutputStream out, final String feature) throws IOException {
        ProtocolFrame.writeResponse(out, ResponseCodes.NOT_IMPLEMENTED,
                feature + " is not available yet on the server.");
    }

    private void ok(final DataOutputStream out, final String body) throws IOException {
        ProtocolFrame.writeResponse(out, ResponseCodes.OK, body);
    }

    private static final class TempFlightPlanFile implements AutoCloseable {

        private final Path path;

        private TempFlightPlanFile(final Path path) {
            this.path = path;
        }

        static TempFlightPlanFile write(final String fileName, final byte[] content) throws IOException {
            final String ext = FlightPlanFileExtensions.normalizeExtension(Path.of(fileName));
            final String suffix = ext.isEmpty() ? ".fp" : "." + ext;
            final Path temp = Files.createTempFile("rcomp-import-", suffix);
            Files.write(temp, content);
            return new TempFlightPlanFile(temp);
        }

        Path path() {
            return path;
        }

        @Override
        public void close() throws IOException {
            Files.deleteIfExists(path);
        }
    }
}
