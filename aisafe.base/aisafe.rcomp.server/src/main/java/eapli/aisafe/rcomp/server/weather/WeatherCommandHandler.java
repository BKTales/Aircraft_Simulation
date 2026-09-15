package eapli.aisafe.rcomp.server.weather;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.WeatherConsultPayload;
import eapli.aisafe.rcomp.protocol.WeatherCsvPayload;
import eapli.aisafe.rcomp.protocol.WeatherOpcodes;
import eapli.aisafe.rcomp.protocol.WeatherRegisterPayload;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.application.BulkImportWeatherResult;
import eapli.aisafe.weatherdata.application.BulkWeatherDataController;
import eapli.aisafe.weatherdata.application.ConsultWeatherResult;
import eapli.aisafe.weatherdata.application.RegisterWeatherDataController;
import eapli.aisafe.weatherdata.application.RegisterWeatherResult;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP command handler for Weather Person remote access (US044): US041–US043.
 */
public final class WeatherCommandHandler {

    private final ClientHandler session;
    private final RegisterWeatherDataController registerController;
    private final BulkWeatherDataController bulkController;

    public WeatherCommandHandler(final ClientHandler session) {
        this(session, new RegisterWeatherDataController(), new BulkWeatherDataController());
    }

    WeatherCommandHandler(final ClientHandler session,
                          final RegisterWeatherDataController registerController,
                          final BulkWeatherDataController bulkController) {
        this.session = session;
        this.registerController = registerController;
        this.bulkController = bulkController;
    }

    public void handle(final byte opcode, final String payload, final DataOutputStream out) throws IOException {
        if (AuthzRegistry.authorizationService().session().isEmpty()) {
            ProtocolFrame.writeResponse(out, ResponseCodes.UNAUTHORIZED, "Login required.");
            return;
        }
        if (!AuthzRegistry.authorizationService().isAuthenticatedUserAuthorizedTo(AISafeRoles.WEATHER_PERSON)) {
            ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "Weather Person role required.");
            return;
        }

        try {
            switch (opcode) {
                case WeatherOpcodes.LIST_AREAS -> listAreas(out);
                case WeatherOpcodes.REGISTER_WEATHER -> registerWeather(payload, out);
                case WeatherOpcodes.BULK_IMPORT -> bulkImport(payload, out);
                case WeatherOpcodes.CONSULT_BY_DAY -> consultByDay(payload, out);
                default -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                        "Unknown Weather opcode: " + opcode);
            }
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, ex.getMessage());
        }
    }

    private void listAreas(final DataOutputStream out) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (final AirControlArea area : registerController.availableAreas()) {
            lines.add(WeatherResponseFormatter.formatArea(area));
        }
        session.logAction(null, "LIST_AREAS");
        ok(out, WeatherResponseFormatter.joinLines(lines));
    }

    private void registerWeather(final String payload, final DataOutputStream out) throws IOException {
        final WeatherRegisterPayload.Fields fields = WeatherRegisterPayload.decode(payload);
        final RegisterWeatherResult result = registerController.registerWeatherData(
                fields.areaCode(),
                fields.coordinates(),
                fields.temperature(),
                fields.direction(),
                fields.speed(),
                fields.humidity(),
                fields.pressure(),
                fields.start(),
                fields.end());
        session.logAction(null, "REGISTER_WEATHER");
        handleRegisterResult(out, result);
    }

    private void bulkImport(final String payload, final DataOutputStream out) throws IOException {
        final WeatherCsvPayload.FileContent file = WeatherCsvPayload.decodeFile(payload);
        try (TempWeatherFile temp = TempWeatherFile.write(file.fileName(), file.bytes())) {
            final BulkImportWeatherResult result = bulkController.importFromFile(temp.path());
            session.logAction(null, "BULK_IMPORT");
            handleBulkImportResult(out, result);
        }
    }

    private void consultByDay(final String payload, final DataOutputStream out) throws IOException {
        final WeatherConsultPayload.Fields fields = WeatherConsultPayload.decode(payload);
        final ConsultWeatherResult result = bulkController.consultWeatherDataForDay(
                fields.areaCode(), fields.day());
        session.logAction(null, "CONSULT_BY_DAY");
        handleConsultResult(out, result);
    }

    private void handleRegisterResult(final DataOutputStream out, final RegisterWeatherResult result) throws IOException {
        if (result.isSuccess()) {
            ok(out, "OK|" + WeatherResponseFormatter.formatWeather(result.weatherData()));
            return;
        }
        switch (result.outcome()) {
            case AREA_NOT_FOUND -> ProtocolFrame.writeResponse(out, ResponseCodes.NOT_FOUND, "Area not found.");
            case SECTION_OUT_OF_BOUNDS -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    "Weather section is outside the area boundary.");
            case INVALID_HUMIDITY -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    "Invalid humidity value.");
            case INVALID_INPUT -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    result.message() != null ? result.message() : "Invalid input.");
            case ERROR -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR,
                    result.message() != null ? result.message() : "Unexpected error.");
            default -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR, "Unexpected error.");
        }
    }

    private void handleBulkImportResult(final DataOutputStream out, final BulkImportWeatherResult result) throws IOException {
        if (result.isSuccess()) {
            ok(out, "Imported " + result.imported().size() + " weather record(s).");
            return;
        }
        switch (result.outcome()) {
            case UNSUPPORTED_FORMAT -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    result.message() != null ? result.message() : "Unsupported file format.");
            case IO_ERROR -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR,
                    result.message() != null ? result.message() : "Error reading file.");
            case AREA_NOT_FOUND -> ProtocolFrame.writeResponse(out, ResponseCodes.NOT_FOUND, "Area not found.");
            case SECTION_OUT_OF_BOUNDS -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    "Weather section is outside the area boundary.");
            case INVALID_HUMIDITY -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    "Invalid humidity value.");
            case INVALID_INPUT -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    result.message() != null ? result.message() : "Invalid input.");
            case ERROR -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR,
                    result.message() != null ? result.message() : "Unexpected error.");
            default -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR, "Unexpected error.");
        }
    }

    private void handleConsultResult(final DataOutputStream out, final ConsultWeatherResult result) throws IOException {
        if (result.isSuccess()) {
            final List<String> lines = result.records().stream()
                    .map(WeatherResponseFormatter::formatWeather)
                    .toList();
            ok(out, WeatherResponseFormatter.joinLines(lines));
            return;
        }
        switch (result.outcome()) {
            case INVALID_INPUT -> ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST,
                    result.message() != null ? result.message() : "Invalid input.");
            case ERROR -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR,
                    result.message() != null ? result.message() : "Unexpected error.");
            default -> ProtocolFrame.writeResponse(out, ResponseCodes.INTERNAL_ERROR, "Unexpected error.");
        }
    }

    private void ok(final DataOutputStream out, final String body) throws IOException {
        ProtocolFrame.writeResponse(out, ResponseCodes.OK, body);
    }

    private static final class TempWeatherFile implements AutoCloseable {

        private final Path path;

        private TempWeatherFile(final Path path) {
            this.path = path;
        }

        static TempWeatherFile write(final String fileName, final byte[] content) throws IOException {
            final String lower = fileName.toLowerCase();
            final String suffix = lower.endsWith(".csv") ? ".csv" : ".weather";
            final Path temp = Files.createTempFile("rcomp-weather-", suffix);
            Files.write(temp, content);
            return new TempWeatherFile(temp);
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
