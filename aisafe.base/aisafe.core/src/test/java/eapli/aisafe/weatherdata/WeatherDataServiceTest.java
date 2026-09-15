package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.domain.*;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.*;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.weatherdata.application.BulkImportWeatherResult;
import eapli.aisafe.weatherdata.application.ConsultWeatherResult;
import eapli.aisafe.weatherdata.application.RegisterWeatherResult;
import eapli.aisafe.weatherdata.application.WeatherDataService;
import eapli.aisafe.weatherdata.application.bulk.UnsupportedWeatherDataFormatException;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReader;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReaderFactory;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkRecord;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherDataServiceTest {

    private AirControlAreaRepository areaRepo;
    private WeatherDataRepository weatherRepo;
    private WeatherDataService service;
    private AirControlArea area;

    @BeforeEach
    void setUp() {
        areaRepo = mock(AirControlAreaRepository.class);
        weatherRepo = mock(WeatherDataRepository.class);
        service = new WeatherDataService(areaRepo, weatherRepo);

        area = new AirControlArea(
                AirControlAreaName.valueOf("Test"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(38.0f, -9.0f), GeographicCoords.valueOf(39.0f, -9.0f),
                        GeographicCoords.valueOf(39.0f, -8.0f), GeographicCoords.valueOf(38.0f, -8.0f)
                )),
                MinFuelRequirement.valueOf(100)
        );
    }

    @Test
    void testRegisterSuccess() {
        stubAreaFoundAndSaveReturnsInput();

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                25.0f, 450, 5.0f, 60.0f, 1013.0f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                null
        );

        assertTrue(result.isSuccess());
        assertEquals(60.0f, result.weatherData().getHumidity().getHumidity(), 0.0001);
        assertEquals(25.0f, result.weatherData().getTemperature().getTemperature(), 0.0001);
        assertEquals(1013.0f, result.weatherData().getPressure().getPressure(), 0.0001);
        assertEquals(90, result.weatherData().getWindData().getDirection().getWindDirection());
        assertEquals(5.0f, result.weatherData().getWindData().getSpeed().getSpeed(), 0.0001);

        verify(areaRepo).ofIdentity(any(AreaCode.class));
        verify(weatherRepo).save(any(WeatherData.class));
    }

    @Test
    void registerCommitsTransactionWhenContextProvided() {
        stubAreaFoundAndSaveReturnsInput();
        final TransactionalContext txCtx = mock(TransactionalContext.class);

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                25.0f, 90, 5.0f, 60.0f, 1013.0f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                txCtx
        );

        assertTrue(result.isSuccess());
        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
        verify(txCtx, never()).rollback();
    }

    @Test
    void registerRollsBackTransactionWhenPersistenceFails() {
        stubAreaFound();
        when(weatherRepo.save(any(WeatherData.class))).thenThrow(new RuntimeException("db down"));
        final TransactionalContext txCtx = mock(TransactionalContext.class);

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                25.0f, 90, 5.0f, 60.0f, 1013.0f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                txCtx
        );

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.ERROR, result.outcome());
        assertEquals("db down", result.message());
        verify(txCtx).beginTransaction();
        verify(txCtx).rollback();
        verify(txCtx).close();
        verify(txCtx, never()).commit();
    }

    @Test
    void testRegisterAreaNotFoundReturnsFailure() {
        when(areaRepo.ofIdentity(any(AreaCode.class))).thenReturn(Optional.empty());

        final RegisterWeatherResult result = service.registerWeatherData(
                "NON-EXISTENT", validCoords(), 20f, 90, 10f, 50f, 1013f,
                LocalDateTime.now(), LocalDateTime.now(), null);

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.AREA_NOT_FOUND, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void testRegisterOutOfBoundsReturnsFailure() {
        stubAreaFound();

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(),
                List.of(new float[]{50f, 10f}, new float[]{51f, 10f}, new float[]{50f, 11f}),
                20f, 0, 0f, 0f, 0f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.SECTION_OUT_OF_BOUNDS, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void testRegisterInvalidHumidityReturnsFailure() {
        stubAreaFound();

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                20f, 0, 10f, 101f, 1013f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.INVALID_HUMIDITY, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void testRegisterInvalidDateRangeReturnsFailure() {
        stubAreaFound();
        final LocalDateTime start = LocalDateTime.now();

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                20f, 0, 10f, 50f, 1013f,
                start, start.minusMinutes(1), null);

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.INVALID_INPUT, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void registerReturnsErrorWhenSaveFailsWithoutTransaction() {
        stubAreaFound();
        when(weatherRepo.save(any(WeatherData.class))).thenThrow(new RuntimeException());

        final RegisterWeatherResult result = service.registerWeatherData(
                area.identity().toString(), validCoords(),
                20f, 90, 10f, 50f, 1013f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);

        assertFalse(result.isSuccess());
        assertEquals(RegisterWeatherResult.Outcome.ERROR, result.outcome());
        assertEquals("RuntimeException", result.message());
    }

    @Test
    void testConsultWeatherDataForDayDelegatesToRepository() {
        final LocalDateTime day = LocalDateTime.of(2026, 6, 22, 10, 0);
        final List<WeatherData> expected = List.of(mock(WeatherData.class));
        when(weatherRepo.findByAreaAndInterval(any(AreaCode.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expected);

        final ConsultWeatherResult result = service.consultWeatherDataForDay("AREA-121", day);

        assertTrue(result.isSuccess());
        assertSame(expected, result.records());
        verify(weatherRepo).findByAreaAndInterval(any(AreaCode.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void consultWeatherDataForDayRejectsNullDay() {
        final ConsultWeatherResult result = service.consultWeatherDataForDay("AREA-121", null);

        assertFalse(result.isSuccess());
        assertEquals(ConsultWeatherResult.Outcome.INVALID_INPUT, result.outcome());
        assertEquals("Day is required.", result.message());
        verifyNoInteractions(weatherRepo);
    }

    @Test
    void consultWeatherDataForDayMapsIllegalArgumentFromRepository() {
        when(weatherRepo.findByAreaAndInterval(any(AreaCode.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("invalid interval"));

        final ConsultWeatherResult result = service.consultWeatherDataForDay(
                "AREA-121", LocalDateTime.of(2026, 6, 22, 10, 0));

        assertFalse(result.isSuccess());
        assertEquals(ConsultWeatherResult.Outcome.INVALID_INPUT, result.outcome());
        assertEquals("invalid interval", result.message());
    }

    @Test
    void consultWeatherDataForDayMapsUnexpectedRepositoryFailures() {
        when(weatherRepo.findByAreaAndInterval(any(AreaCode.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("repository offline"));

        final ConsultWeatherResult result = service.consultWeatherDataForDay(
                "AREA-121", LocalDateTime.of(2026, 6, 22, 10, 0));

        assertFalse(result.isSuccess());
        assertEquals(ConsultWeatherResult.Outcome.ERROR, result.outcome());
        assertEquals("repository offline", result.message());
    }

    @Test
    void importFromFileImportsEveryRecord() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(validBulkRecord(), validBulkRecord()));
        stubAreaFoundAndSaveReturnsInput();

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, null);

        assertTrue(result.isSuccess());
        assertEquals(2, result.imported().size());
        verify(weatherRepo, times(2)).save(any(WeatherData.class));
    }

    @Test
    void importFromFileCommitsTransactionWhenContextProvided() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final TransactionalContext txCtx = mock(TransactionalContext.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(validBulkRecord()));
        stubAreaFoundAndSaveReturnsInput();

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, txCtx);

        assertTrue(result.isSuccess());
        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
        verify(txCtx, never()).rollback();
    }

    @Test
    void importFromFileReturnsUnsupportedFormatOutcome() {
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class)))
                .thenThrow(new UnsupportedWeatherDataFormatException(Path.of("weather.xml")));

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.xml"), factory, null);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.UNSUPPORTED_FORMAT, result.outcome());
        assertEquals("Unsupported weather data file format: weather.xml", result.message());
        verifyNoInteractions(weatherRepo);
    }

    @Test
    void importFromFileReturnsIoErrorOutcome() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenThrow(new IOException("broken file"));

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, null);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.IO_ERROR, result.outcome());
        assertEquals("broken file", result.message());
        verifyNoInteractions(weatherRepo);
    }

    @Test
    void importFromFileReturnsAreaNotFoundOutcome() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(
                new WeatherDataBulkRecord(
                        "MISSING", validCoords(), 20f, 90, 10f, 50f, 1013f,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1))));
        when(areaRepo.ofIdentity(any(AreaCode.class))).thenReturn(Optional.empty());

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, null);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.AREA_NOT_FOUND, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void importFromFileReturnsSectionOutOfBoundsOutcome() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(
                new WeatherDataBulkRecord(
                        area.identity().toString(),
                        List.of(new float[]{50f, 10f}, new float[]{51f, 10f}, new float[]{50f, 11f}),
                        20f, 90, 10f, 50f, 1013f,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1))));
        stubAreaFound();

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, null);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.SECTION_OUT_OF_BOUNDS, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void importFromFileReturnsInvalidHumidityOutcome() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(
                new WeatherDataBulkRecord(
                        area.identity().toString(), validCoords(),
                        20f, 90, 10f, 101f, 1013f,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1))));
        stubAreaFound();

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, null);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.INVALID_HUMIDITY, result.outcome());
        verify(weatherRepo, never()).save(any(WeatherData.class));
    }

    @Test
    void importFromFileRollsBackTransactionWhenRegistrationFails() throws IOException {
        final WeatherDataBulkReader reader = mock(WeatherDataBulkReader.class);
        final WeatherDataBulkReaderFactory factory = mock(WeatherDataBulkReaderFactory.class);
        final TransactionalContext txCtx = mock(TransactionalContext.class);
        when(factory.forFile(any(Path.class))).thenReturn(reader);
        when(reader.read(any(Path.class))).thenReturn(List.of(validBulkRecord()));
        stubAreaFound();
        when(weatherRepo.save(any(WeatherData.class))).thenThrow(new RuntimeException("db down"));

        final BulkImportWeatherResult result = service.importFromFile(Path.of("weather.csv"), factory, txCtx);

        assertFalse(result.isSuccess());
        assertEquals(BulkImportWeatherResult.Outcome.ERROR, result.outcome());
        assertEquals("db down", result.message());
        verify(txCtx).beginTransaction();
        verify(txCtx).rollback();
        verify(txCtx).close();
        verify(txCtx, never()).commit();
    }

    private void stubAreaFound() {
        when(areaRepo.ofIdentity(any(AreaCode.class))).thenReturn(Optional.of(area));
    }

    private void stubAreaFoundAndSaveReturnsInput() {
        stubAreaFound();
        when(weatherRepo.save(any(WeatherData.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private List<float[]> validCoords() {
        return List.of(
                new float[]{38.5f, -8.5f},
                new float[]{38.6f, -8.5f},
                new float[]{38.5f, -8.4f}
        );
    }

    private WeatherDataBulkRecord validBulkRecord() {
        return new WeatherDataBulkRecord(
                area.identity().toString(),
                validCoords(),
                25.0f, 90, 5.0f, 60.0f, 1013.0f,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        );
    }
}
