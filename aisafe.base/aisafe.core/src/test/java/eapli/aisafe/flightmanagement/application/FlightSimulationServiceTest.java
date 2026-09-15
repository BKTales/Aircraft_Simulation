package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.flightmanagement.application.exceptions.NoEligibleFlightsException;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryReportGenerator;
import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;
import eapli.aisafe.flightmanagement.infrastructure.simulator.FlightSimulatorPaths;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorGateway;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorLaunchResult;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorWeatherSnapshotExporter;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.weatherdata.domain.Humidity;
import eapli.aisafe.weatherdata.domain.Pressure;
import eapli.aisafe.weatherdata.domain.Temperature;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.domain.WeatherDate;
import eapli.aisafe.weatherdata.domain.WeatherSection;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightSimulationServiceTest {

    private FlightRepository flightRepository;
    private FlightEligibilityService eligibilityService;
    private SimulationReportParser reportParser;
    private SimulatorGateway simulatorGateway;
    private FlightSimulatorPaths simulatorPaths;
    private FlightSimulationService service;

    private FlightSimulationService serviceFor(final Path reportsBaseDir) {
        return new FlightSimulationService(
                flightRepository,
                eligibilityService,
                reportParser,
                simulatorGateway,
                simulatorPaths,
                null,
                null,
                new FlightDslParser(),
                new FlightPlanAreaClipper(),
                new SimulationSummaryReportGenerator(new SimulationReportsPathResolver(reportsBaseDir)),
                null,
                new SimulatorWeatherSnapshotExporter());
    }

    @BeforeEach
    void setUp(@TempDir final Path tempDir) {
        flightRepository = mock(FlightRepository.class);
        eligibilityService = mock(FlightEligibilityService.class);
        reportParser = new SimulationReportParser();
        simulatorGateway = mock(SimulatorGateway.class);
        simulatorPaths = mock(FlightSimulatorPaths.class);
        service = serviceFor(tempDir.resolve("reports"));
    }

    @Test
    void ensureNoEligibleFlightsThrows(@TempDir final Path tempDir) throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 18, 0);
        when(flightRepository.findScheduledWithFlightPlan(start, end)).thenReturn(List.of(sampleFlight()));
        when(eligibilityService.filterEligible(any(), eq("AREA-0"), eq(start), eq(end))).thenReturn(List.of());

        assertThrows(NoEligibleFlightsException.class,
                () -> service.simulateFlightsInArea("AREA-0", start, end));
    }

    @Test
    void ensureSimulationReturnsParsedResult(@TempDir final Path tempDir) throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 18, 0);
        final Flight flight = sampleFlight();
        final Path workDir = tempDir.resolve("main");
        final Path binary = tempDir.resolve("flight_simulator");
        Files.createDirectories(workDir);
        Files.createFile(binary);
        Files.setPosixFilePermissions(binary, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));

        when(flightRepository.findScheduledWithFlightPlan(start, end)).thenReturn(List.of(flight));
        when(eligibilityService.filterEligible(any(), eq("AREA-0"), eq(start), eq(end))).thenReturn(List.of(flight));
        when(eligibilityService.crossingSpanFor(any(), eq("AREA-0"))).thenReturn(Optional.of(
                new AreaCrossingSpan(0, 0.0, 1.0, 41.0, -8.0, 38.0, -9.0)));
        when(simulatorPaths.simulatorBinary()).thenReturn(binary);
        when(simulatorPaths.simulatorWorkingDirectory()).thenReturn(workDir);
        when(simulatorGateway.launch(eq(binary), eq(workDir), any(Map.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final Map<String, String> env = invocation.getArgument(2);
            assertTrue(env.containsKey("FS_WEATHER_FILE"));
            assertTrue(Files.isRegularFile(Path.of(env.get("FS_WEATHER_FILE"))));
            final Path reportsDir = Path.of(env.get("FS_REPORTS_DIR"));
            Files.createDirectories(reportsDir);
            Files.writeString(reportsDir.resolve("report.csv"), "metric,value\nvalidation_result,PASS\n");
            return new SimulatorLaunchResult(0, List.of("[INIT] Loaded 1 flight plan(s)"));
        });

        final SimulationResult result = service.simulateFlightsInArea("AREA-0", start, end);

        assertTrue(result.passed());
        assertEquals(1, result.exportedPlans());
        assertTrue(Files.isDirectory(Path.of(result.flightPlansDirectory())));
        assertTrue(Files.isRegularFile(Path.of(result.flightPlansDirectory(), "weather_snapshot.json")));
        assertTrue(result.hasSummary());
        assertTrue(result.summaryContent().isPresent());
        assertTrue(Files.isRegularFile(Path.of(result.summaryReportPath().orElseThrow())));
    }

    @Test
    void ensureSimulateEligibleFlightsRunsWithoutRepositoryLookup(@TempDir final Path tempDir) throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 18, 0);
        final Flight flight = sampleFlight();
        final Path workDir = tempDir.resolve("main");
        final Path binary = tempDir.resolve("flight_simulator");
        Files.createDirectories(workDir);
        Files.createFile(binary);
        Files.setPosixFilePermissions(binary, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));

        when(simulatorPaths.simulatorBinary()).thenReturn(binary);
        when(simulatorPaths.simulatorWorkingDirectory()).thenReturn(workDir);
        when(simulatorGateway.launch(eq(binary), eq(workDir), any(Map.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final Map<String, String> env = invocation.getArgument(2);
            final Path reportsDir = Path.of(env.get("FS_REPORTS_DIR"));
            Files.createDirectories(reportsDir);
            Files.writeString(reportsDir.resolve("report.csv"), "metric,value\nvalidation_result,PASS\n");
            return new SimulatorLaunchResult(0, List.of());
        });

        final SimulationResult result = service.simulateEligibleFlights("AREA-0", start, end, List.of(flight));

        assertTrue(result.passed());
        assertEquals(1, result.exportedPlans());
        assertFalse(result.hasSummary());
    }

    @Test
    void ensureSimulatorNonZeroExitFails(@TempDir final Path tempDir) throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 18, 0);
        final Flight flight = sampleFlight();
        final Path workDir = tempDir.resolve("main");
        final Path binary = tempDir.resolve("flight_simulator");
        Files.createDirectories(workDir);
        Files.createFile(binary);
        Files.setPosixFilePermissions(binary, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));

        when(flightRepository.findScheduledWithFlightPlan(start, end)).thenReturn(List.of(flight));
        when(eligibilityService.filterEligible(any(), eq("AREA-0"), eq(start), eq(end))).thenReturn(List.of(flight));
        when(eligibilityService.crossingSpanFor(any(), eq("AREA-0"))).thenReturn(Optional.of(
                new AreaCrossingSpan(0, 0.0, 1.0, 41.0, -8.0, 38.0, -9.0)));
        when(simulatorPaths.simulatorBinary()).thenReturn(binary);
        when(simulatorPaths.simulatorWorkingDirectory()).thenReturn(workDir);
        when(simulatorGateway.launch(eq(binary), eq(workDir), any(Map.class)))
                .thenReturn(new SimulatorLaunchResult(1, List.of()));

        assertThrows(SimulatorExecutionException.class,
                () -> service.simulateFlightsInArea("AREA-0", start, end));
    }

    @Test
    void ensureDifferentWeatherChangesSnapshotAndOutcomeWithSamePlan(@TempDir final Path tempDir) throws Exception {
        final String[] calmSnapshot = new String[1];
        final String[] calmPlan = new String[1];
        final SimulationResult calm = runWithWeather(tempDir.resolve("calm"), 3.0, calmSnapshot, calmPlan);

        final String[] windySnapshot = new String[1];
        final String[] windyPlan = new String[1];
        final SimulationResult windy = runWithWeather(tempDir.resolve("windy"), 30.0, windySnapshot, windyPlan);

        assertTrue(calm.passed(), "calm wind should pass simulation");
        assertFalse(windy.passed(), "strong wind should fail simulation");

        assertNotEquals(calmSnapshot[0], windySnapshot[0], "weather snapshot must differ between weathers");
        assertTrue(calmSnapshot[0].contains("\"windSpeedMs\": 3"));
        assertTrue(windySnapshot[0].contains("\"windSpeedMs\": 30"));

        assertEquals(calmPlan[0], windyPlan[0], "same route/aircraft/weight => identical flight plan JSON");
    }

    private SimulationResult runWithWeather(final Path base,
                                            final double windSpeedMs,
                                            final String[] snapshotOut,
                                            final String[] planOut) throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 18, 0);
        final Flight flight = sampleFlight();
        flight.assignWeatherData(sampleWeather(42L,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                270, windSpeedMs));

        final Path workDir = base.resolve("main");
        final Path binary = base.resolve("flight_simulator");
        Files.createDirectories(workDir);
        Files.createFile(binary);
        Files.setPosixFilePermissions(binary, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));

        when(simulatorPaths.simulatorBinary()).thenReturn(binary);
        when(simulatorPaths.simulatorWorkingDirectory()).thenReturn(workDir);
        when(simulatorGateway.launch(eq(binary), eq(workDir), any(Map.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final Map<String, String> env = invocation.getArgument(2);
            final Path weatherFile = Path.of(env.get("FS_WEATHER_FILE"));
            snapshotOut[0] = Files.readString(weatherFile);
            try (Stream<Path> entries = Files.list(weatherFile.getParent())) {
                final Path plan = entries
                        .filter(p -> p.getFileName().toString().startsWith("flight_plan_"))
                        .findFirst()
                        .orElseThrow();
                planOut[0] = Files.readString(plan);
            }
            final String verdict = parseWindSpeed(snapshotOut[0]) >= 10.0 ? "FAIL" : "PASS";
            final Path reportsDir = Path.of(env.get("FS_REPORTS_DIR"));
            Files.createDirectories(reportsDir);
            Files.writeString(reportsDir.resolve("report.csv"), "metric,value\nvalidation_result," + verdict + "\n");
            return new SimulatorLaunchResult(0, List.of());
        });

        return service.simulateEligibleFlights("AREA-0", start, end, List.of(flight));
    }

    private static double parseWindSpeed(final String snapshot) {
        final Matcher matcher = Pattern.compile("\"windSpeedMs\":\\s*([0-9.]+)").matcher(snapshot);
        if (!matcher.find()) {
            throw new IllegalStateException("windSpeedMs not found in snapshot: " + snapshot);
        }
        return Double.parseDouble(matcher.group(1));
    }

    private static WeatherData sampleWeather(final Long id,
                                             final LocalDateTime start,
                                             final LocalDateTime end,
                                             final int directionDeg,
                                             final double speedMs) throws Exception {
        final WeatherSection section = new WeatherSection(new GeographicBoundary(List.of(
                GeographicCoords.valueOf(37.0, -9.5),
                GeographicCoords.valueOf(37.0, -8.0),
                GeographicCoords.valueOf(38.0, -8.0),
                GeographicCoords.valueOf(38.0, -9.5))));
        final AirControlArea area = new AirControlArea(
                new AirControlAreaName("Lisboa"),
                new GeographicBoundary(List.of(
                        GeographicCoords.valueOf(36.0, -10.0),
                        GeographicCoords.valueOf(36.0, -7.0),
                        GeographicCoords.valueOf(39.0, -7.0),
                        GeographicCoords.valueOf(39.0, -10.0))),
                new MinFuelRequirement(100));
        final WeatherData weather = new WeatherData(
                new WeatherDate(start, end),
                new Humidity(50),
                new Pressure(1013),
                new Temperature(20),
                new WindData(WindDataDirection.valueOf(directionDeg), WindDataSpeed.valueOf(speedMs)),
                section,
                area);
        final Field field = WeatherData.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(weather, id);
        return weather;
    }

    private static Flight sampleFlight() {
        final Flight flight = new Flight(
                new FlightDesignator("TP100"),
                "ROUTE-1",
                "CS-DEMO");
        flight.assignSchedule(FlightSchedule.valueOf(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0)));
        flight.assignFlightPlan(FlightPlan.forFlight(
                flight.designator(),
                FlightPlanStatus.DRAFT,
                FuelLoad.valueOf(1000.0),
                SimulatorJsonTestFixtures.minimalSelfContainedJson("AREA-0")));
        return flight;
    }
}
