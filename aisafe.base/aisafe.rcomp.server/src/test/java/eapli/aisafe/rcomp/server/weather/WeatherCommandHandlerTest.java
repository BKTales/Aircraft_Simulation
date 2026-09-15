package eapli.aisafe.rcomp.server.weather;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.WeatherConsultPayload;
import eapli.aisafe.rcomp.protocol.WeatherCsvPayload;
import eapli.aisafe.rcomp.protocol.WeatherOpcodes;
import eapli.aisafe.rcomp.protocol.WeatherRegisterPayload;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.rcomp.server.ClientHandler;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.application.BulkImportWeatherResult;
import eapli.aisafe.weatherdata.application.BulkWeatherDataController;
import eapli.aisafe.weatherdata.application.ConsultWeatherResult;
import eapli.aisafe.weatherdata.application.RegisterWeatherDataController;
import eapli.aisafe.weatherdata.application.RegisterWeatherResult;
import eapli.aisafe.weatherdata.domain.Humidity;
import eapli.aisafe.weatherdata.domain.Pressure;
import eapli.aisafe.weatherdata.domain.Temperature;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.domain.WeatherDate;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeatherCommandHandlerTest {

    private TestAuthorizationService authz;
    @Mock
    private UserSession session;
    @Mock
    private ClientHandler clientSession;
    @Mock
    private RegisterWeatherDataController registerController;
    @Mock
    private BulkWeatherDataController bulkController;

    private WeatherCommandHandler handler;

    @BeforeEach
    void setUp() {
        authz = new TestAuthorizationService();
        authz.sessionOpt = Optional.of(session);
        authz.weatherAuthorized = true;
        configureAuthzRegistry(authz);
        when(session.authenticatedUser()).thenReturn(mock(SystemUser.class));

        handler = new WeatherCommandHandler(clientSession, registerController, bulkController);
    }

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
    }

    @Test
    void ensureRequiresSession() throws Exception {
        authz.sessionOpt = Optional.empty();
        final ProtocolFrame response = invoke(WeatherOpcodes.LIST_AREAS, "");
        assertEquals(ResponseCodes.UNAUTHORIZED, response.opcode());
    }

    @Test
    void ensureRequiresWeatherRole() throws Exception {
        authz.weatherAuthorized = false;
        final ProtocolFrame response = invoke(WeatherOpcodes.LIST_AREAS, "");
        assertEquals(ResponseCodes.FORBIDDEN, response.opcode());
    }

    @Test
    void ensureListAreasReturnsCodes() throws Exception {
        final AirControlArea area = mock(AirControlArea.class);
        final var boundary = mock(eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary.class);
        when(area.identity()).thenReturn(AreaCode.valueOf("AREA-0"));
        when(area.getGeographicBoundary()).thenReturn(boundary);
        when(boundary.getGeoCords()).thenReturn(List.of(new GeographicCoords(1f, 2f)));
        when(registerController.availableAreas()).thenReturn(List.of(area));

        final ProtocolFrame response = invoke(WeatherOpcodes.LIST_AREAS, "");

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().contains("AREA-0|1.0:2.0"));
    }

    @Test
    void ensureRegisterWeatherSucceeds() throws Exception {
        final WeatherData weather = mockWeather(1L, "AREA-0");
        when(registerController.registerWeatherData(
                eq("AREA-0"), anyList(), eq(20f), eq(180), eq(10f), eq(50f), eq(1013f),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(RegisterWeatherResult.success(weather));

        final String payload = WeatherRegisterPayload.encode(new WeatherRegisterPayload.Fields(
                "AREA-0", 20f, 50f, 1013f, 180, 10f,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                List.of(new float[]{1f, 1f}, new float[]{2f, 1f}, new float[]{1f, 2f})));

        final ProtocolFrame response = invoke(WeatherOpcodes.REGISTER_WEATHER, payload);

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().startsWith("OK|"));
        assertTrue(response.payload().contains("AREA-0"));
    }

    @Test
    void ensureBulkImportSucceeds() throws Exception {
        final WeatherData imported = mockWeather(2L, "AREA-0");
        when(bulkController.importFromFile(any(Path.class)))
                .thenReturn(BulkImportWeatherResult.success(List.of(imported)));

        final String payload = WeatherCsvPayload.encodeFile(
                "weather.csv", "area,temp\nAREA-0,20".getBytes(StandardCharsets.UTF_8));
        final ProtocolFrame response = invoke(WeatherOpcodes.BULK_IMPORT, payload);

        assertEquals(ResponseCodes.OK, response.opcode());
        assertEquals("Imported 1 weather record(s).", response.payload());
    }

    @Test
    void ensureUnknownOpcodeReturnsBadRequest() throws Exception {
        final ProtocolFrame response = invoke((byte) 59, "");
        assertEquals(ResponseCodes.BAD_REQUEST, response.opcode());
        assertTrue(response.payload().contains("Unknown Weather opcode"));
    }

    @Test
    void ensureInvalidRegisterPayloadReturnsBadRequest() throws Exception {
        final ProtocolFrame response = invoke(WeatherOpcodes.REGISTER_WEATHER, "invalid");
        assertEquals(ResponseCodes.BAD_REQUEST, response.opcode());
    }

    @Test
    void ensureConsultByDayReturnsEmptyList() throws Exception {
        when(bulkController.consultWeatherDataForDay(eq("AREA-0"), any(LocalDateTime.class)))
                .thenReturn(ConsultWeatherResult.success(List.of()));

        final ProtocolFrame response = invoke(
                WeatherOpcodes.CONSULT_BY_DAY,
                WeatherConsultPayload.encode("AREA-0", LocalDate.of(2026, 6, 1)));

        assertEquals(ResponseCodes.OK, response.opcode());
        assertEquals("", response.payload());
    }

    @Test
    void ensureRegisterFailureReturnsBadRequest() throws Exception {
        when(registerController.registerWeatherData(
                any(), anyList(), anyFloat(), anyInt(), anyFloat(),
                anyFloat(), anyFloat(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(RegisterWeatherResult.failureWithMsg(RegisterWeatherResult.Outcome.ERROR, "domain error"));

        final String payload = WeatherRegisterPayload.encode(new WeatherRegisterPayload.Fields(
                "AREA-0", 20f, 50f, 1013f, 180, 10f,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                List.of(new float[]{1f, 1f}, new float[]{2f, 1f}, new float[]{1f, 2f})));

        final ProtocolFrame response = invoke(WeatherOpcodes.REGISTER_WEATHER, payload);
        assertEquals(ResponseCodes.INTERNAL_ERROR, response.opcode());
        assertEquals("domain error", response.payload());
    }

    @Test
    void ensureBulkImportFailureReturnsBadRequest() throws Exception {
        when(bulkController.importFromFile(any(Path.class)))
                .thenReturn(BulkImportWeatherResult.failureWithMsg(BulkImportWeatherResult.Outcome.ERROR, "import failed"));

        final String payload = WeatherCsvPayload.encodeFile(
                "weather.csv", "x".getBytes(StandardCharsets.UTF_8));
        final ProtocolFrame response = invoke(WeatherOpcodes.BULK_IMPORT, payload);

        assertEquals(ResponseCodes.INTERNAL_ERROR, response.opcode());
        assertEquals("import failed", response.payload());
    }

    @Test
    void ensureConsultByDayReturnsRecords() throws Exception {
        final WeatherData weather = mockWeather(7L, "AREA-1");
        when(bulkController.consultWeatherDataForDay(eq("AREA-1"), any(LocalDateTime.class)))
                .thenReturn(ConsultWeatherResult.success(List.of(weather)));

        final ProtocolFrame response = invoke(
                WeatherOpcodes.CONSULT_BY_DAY,
                WeatherConsultPayload.encode("AREA-1", LocalDate.of(2026, 6, 1)));

        assertEquals(ResponseCodes.OK, response.opcode());
        assertTrue(response.payload().contains("7|AREA-1"));
    }

    private static WeatherData mockWeather(final long id, final String areaCode) {
        final WeatherData weather = mock(WeatherData.class);
        final AirControlArea area = mock(AirControlArea.class);
        when(weather.identity()).thenReturn(id);
        when(weather.getAirControlArea()).thenReturn(area);
        when(area.identity()).thenReturn(AreaCode.valueOf(areaCode));
        when(weather.getWeatherDate()).thenReturn(new WeatherDate(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0)));
        when(weather.getTemperature()).thenReturn(new Temperature(20f));
        when(weather.getHumidity()).thenReturn(new Humidity(50f));
        when(weather.getPressure()).thenReturn(new Pressure(1013f));
        when(weather.getWindData()).thenReturn(new WindData(
                new WindDataDirection(180), new WindDataSpeed(10f)));
        return weather;
    }

    private ProtocolFrame invoke(final byte opcode, final String payload) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        handler.handle(opcode, payload, new DataOutputStream(baos));
        return ProtocolFrame.read(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
    }

    private static void configureAuthzRegistry(final AuthorizationService authz) {
        try {
            final Field field = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            field.setAccessible(true);
            field.set(null, authz);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void resetAuthzRegistry() {
        try {
            final Field field = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            field.setAccessible(true);
            field.set(null, null);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class TestAuthorizationService extends AuthorizationService {
        private Optional<UserSession> sessionOpt = Optional.empty();
        private boolean weatherAuthorized = true;

        @Override
        public Optional<UserSession> session() {
            return sessionOpt;
        }

        @Override
        public boolean isAuthenticatedUserAuthorizedTo(final Role... roles) {
            if (roles == null) {
                return false;
            }
            for (final Role role : roles) {
                if (AISafeRoles.WEATHER_PERSON.equals(role)) {
                    return weatherAuthorized;
                }
            }
            return false;
        }
    }
}
