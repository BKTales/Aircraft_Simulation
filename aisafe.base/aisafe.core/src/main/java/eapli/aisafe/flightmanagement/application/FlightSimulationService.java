package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryReportGenerator;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryRequest;
import eapli.aisafe.flightmanagement.application.reporting.SimulationSummaryResult;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightmanagement.application.exceptions.NoEligibleFlightsException;
import eapli.aisafe.flightmanagement.application.exceptions.SimulatorExecutionException;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.infrastructure.simulator.FlightSimulatorPaths;
import eapli.aisafe.flightmanagement.infrastructure.simulator.ProcessSimulatorGateway;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorFlightDesignatorMap;
import eapli.aisafe.flightmanagement.infrastructure.simulator.Lapr4FlightPlanJson;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorFlightId;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorGateway;
import eapli.aisafe.flightmanagement.infrastructure.simulator.SimulatorWeatherSnapshotExporter;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FlightSimulationService {

    private final FlightRepository flightRepository;
    private final FlightEligibilityService eligibilityService;
    private final SimulationReportParser reportParser;
    private final SimulatorGateway simulatorGateway;
    private final FlightSimulatorPaths simulatorPaths;
    private final SimulatorFlightPlanJsonBuilder jsonBuilder;
    private final AircraftRepository aircraftRepository;
    private final FlightDslParser dslParser;
    private final FlightPlanAreaClipper areaClipper;
    private final SimulationSummaryReportGenerator summaryGenerator;
    private final WeatherDataRepository weatherDataRepository;
    private final SimulatorWeatherSnapshotExporter weatherExporter;

    public FlightSimulationService(final FlightRepository flightRepository) {
        this(flightRepository, new FlightEligibilityService(), new SimulationReportParser(),
                new ProcessSimulatorGateway(), new FlightSimulatorPaths(),
                null, null, new FlightDslParser(), new FlightPlanAreaClipper(),
                new SimulationSummaryReportGenerator(), null, null);
    }

    public FlightSimulationService(final FlightRepository flightRepository,
                                   final AirControlAreaRepository areaRepository,
                                   final AircraftRepository aircraftRepository,
                                   final AircraftModelRepository aircraftModels,
                                   final EngineModelRepository engineModels,
                                   final AirportRepository airports) {
        this(flightRepository, areaRepository, aircraftRepository, aircraftModels, engineModels, airports, null);
    }

    public FlightSimulationService(final FlightRepository flightRepository,
                                   final AirControlAreaRepository areaRepository,
                                   final AircraftRepository aircraftRepository,
                                   final AircraftModelRepository aircraftModels,
                                   final EngineModelRepository engineModels,
                                   final AirportRepository airports,
                                   final WeatherDataRepository weatherDataRepository) {
        this(flightRepository,
                new FlightEligibilityService(areaRepository, new FlightDslParser(),
                        new eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector()),
                new SimulationReportParser(),
                new ProcessSimulatorGateway(),
                new FlightSimulatorPaths(),
                aircraftRepository,
                new SimulatorFlightPlanJsonBuilder(aircraftModels, engineModels, airports),
                new FlightDslParser(),
                new FlightPlanAreaClipper(),
                new SimulationSummaryReportGenerator(),
                weatherDataRepository,
                new SimulatorWeatherSnapshotExporter());
    }

    FlightSimulationService(final FlightRepository flightRepository,
                            final FlightEligibilityService eligibilityService,
                            final SimulationReportParser reportParser,
                            final SimulatorGateway simulatorGateway,
                            final FlightSimulatorPaths simulatorPaths) {
        this(flightRepository, eligibilityService, reportParser, simulatorGateway, simulatorPaths,
                null, null, new FlightDslParser(), new FlightPlanAreaClipper(),
                new SimulationSummaryReportGenerator(), null, null);
    }

    FlightSimulationService(final FlightRepository flightRepository,
                            final FlightEligibilityService eligibilityService,
                            final SimulationReportParser reportParser,
                            final SimulatorGateway simulatorGateway,
                            final FlightSimulatorPaths simulatorPaths,
                            final AircraftRepository aircraftRepository,
                            final SimulatorFlightPlanJsonBuilder jsonBuilder,
                            final FlightDslParser dslParser,
                            final FlightPlanAreaClipper areaClipper) {
        this(flightRepository, eligibilityService, reportParser, simulatorGateway, simulatorPaths,
                aircraftRepository, jsonBuilder, dslParser, areaClipper,
                new SimulationSummaryReportGenerator(), null, null);
    }

    FlightSimulationService(final FlightRepository flightRepository,
                            final FlightEligibilityService eligibilityService,
                            final SimulationReportParser reportParser,
                            final SimulatorGateway simulatorGateway,
                            final FlightSimulatorPaths simulatorPaths,
                            final AircraftRepository aircraftRepository,
                            final SimulatorFlightPlanJsonBuilder jsonBuilder,
                            final FlightDslParser dslParser,
                            final FlightPlanAreaClipper areaClipper,
                            final SimulationSummaryReportGenerator summaryGenerator) {
        this(flightRepository, eligibilityService, reportParser, simulatorGateway, simulatorPaths,
                aircraftRepository, jsonBuilder, dslParser, areaClipper, summaryGenerator, null, null);
    }

    FlightSimulationService(final FlightRepository flightRepository,
                            final FlightEligibilityService eligibilityService,
                            final SimulationReportParser reportParser,
                            final SimulatorGateway simulatorGateway,
                            final FlightSimulatorPaths simulatorPaths,
                            final AircraftRepository aircraftRepository,
                            final SimulatorFlightPlanJsonBuilder jsonBuilder,
                            final FlightDslParser dslParser,
                            final FlightPlanAreaClipper areaClipper,
                            final SimulationSummaryReportGenerator summaryGenerator,
                            final WeatherDataRepository weatherDataRepository,
                            final SimulatorWeatherSnapshotExporter weatherExporter) {
        this.flightRepository = Objects.requireNonNull(flightRepository, "flightRepository");
        this.eligibilityService = Objects.requireNonNull(eligibilityService, "eligibilityService");
        this.reportParser = Objects.requireNonNull(reportParser, "reportParser");
        this.simulatorGateway = Objects.requireNonNull(simulatorGateway, "simulatorGateway");
        this.simulatorPaths = Objects.requireNonNull(simulatorPaths, "simulatorPaths");
        this.aircraftRepository = aircraftRepository;
        this.jsonBuilder = jsonBuilder;
        this.dslParser = Objects.requireNonNull(dslParser, "dslParser");
        this.areaClipper = Objects.requireNonNull(areaClipper, "areaClipper");
        this.summaryGenerator = Objects.requireNonNull(summaryGenerator, "summaryGenerator");
        this.weatherDataRepository = weatherDataRepository;
        this.weatherExporter = weatherExporter;
    }

    public List<EligibleFlightPreview> listEligibleForArea(final String areaCode,
                                                         final LocalDateTime intervalStart,
                                                         final LocalDateTime intervalEnd) {
        validateInterval(areaCode, intervalStart, intervalEnd);
        final List<Flight> scheduled = flightRepository.findScheduledWithFlightPlan(intervalStart, intervalEnd);
        return eligibilityService.filterEligible(scheduled, areaCode, intervalStart, intervalEnd).stream()
                .map(flight -> new EligibleFlightPreview(
                        flight.identity().toString(),
                        eligibilityService.clipModeFor(flight, areaCode).orElse(AreaClipMode.FULL)))
                .toList();
    }

    public SimulationResult simulateFlightsInArea(final String areaCode,
                                                    final LocalDateTime intervalStart,
                                                    final LocalDateTime intervalEnd) {
        validateInterval(areaCode, intervalStart, intervalEnd);

        final List<Flight> scheduled = flightRepository.findScheduledWithFlightPlan(intervalStart, intervalEnd);
        final List<Flight> eligible = eligibilityService.filterEligible(scheduled, areaCode, intervalStart, intervalEnd);
        if (eligible.isEmpty()) {
            throw new NoEligibleFlightsException(areaCode, intervalStart, intervalEnd);
        }
        return runSimulation(areaCode, eligible, true, intervalStart, intervalEnd, true, null);
    }

    public SimulationResult simulateEligibleFlights(final String areaCode,
                                                    final LocalDateTime intervalStart,
                                                    final LocalDateTime intervalEnd,
                                                    final List<Flight> eligible) {
        return simulateEligibleFlights(areaCode, intervalStart, intervalEnd, eligible, null);
    }

    public SimulationResult simulateEligibleFlights(final String areaCode,
                                                    final LocalDateTime intervalStart,
                                                    final LocalDateTime intervalEnd,
                                                    final List<Flight> eligible,
                                                    final String targetDesignator) {
        validateInterval(areaCode, intervalStart, intervalEnd);
        if (eligible == null || eligible.isEmpty()) {
            throw new IllegalArgumentException("At least one eligible flight is required.");
        }
        return runSimulation(areaCode, eligible, false, null, null, false, targetDesignator);
    }

    private SimulationResult runSimulation(final String areaCode,
                                           final List<Flight> eligible,
                                           final boolean clipToArea,
                                           final LocalDateTime intervalStart,
                                           final LocalDateTime intervalEnd,
                                           final boolean persistSummary,
                                           final String targetDesignator) {
        final Path plansDir;
        final Path reportsDir;
        try {
            plansDir = Files.createTempDirectory("aisafe-flight-plans-");
            reportsDir = Files.createTempDirectory("aisafe-sim-reports-");
            for (final Flight flight : eligible) {
                final String fileName = "flight_plan_" + flight.identity() + ".json";
                final String json = clipToArea
                        ? simulatorJsonClippedForArea(flight, areaCode)
                        : simulatorJsonFor(flight);
                Files.writeString(plansDir.resolve(fileName), json);
            }
        } catch (final IOException ex) {
            throw new SimulatorExecutionException("Failed to prepare flight plan files for simulation.", ex);
        }

        final Map<String, String> env = new HashMap<>();
        env.put("FS_FLIGHT_PLANS_DIR", plansDir.toAbsolutePath().toString());
        env.put("FS_REPORTS_DIR", reportsDir.toAbsolutePath().toString());
        env.put("FS_NON_INTERACTIVE", "1");
        try {
            exportWeatherSnapshot(areaCode, eligible, intervalStart, intervalEnd, plansDir, env);
        } catch (final IOException ex) {
            throw new SimulatorExecutionException("Failed to prepare weather snapshot for simulation.", ex);
        }
        if (targetDesignator != null && !targetDesignator.isBlank()) {
            env.put("FS_TARGET_FLIGHT_ID", resolveSimulatorTargetFlightId(eligible, targetDesignator.trim()));
        }

        final var launch = simulatorGateway.launch(
                simulatorPaths.simulatorBinary(),
                simulatorPaths.simulatorWorkingDirectory(),
                env);
        final int exitCode = launch.exitCode();

        final Path reportCsv = reportsDir.resolve("report.csv");
        final Path reportTxt = reportsDir.resolve("report.txt");

        if (exitCode != 0) {
            final List<String> outputLines = launch.outputLines();
            throw new SimulatorExecutionException(
                    SimulatorFailureMessages.summarize(outputLines, exitCode),
                    outputLines);
        }
        if (!Files.isRegularFile(reportCsv)) {
            throw new SimulatorExecutionException("Simulation report not found at " + reportCsv);
        }

        try {
            final FlightSimulationReport report = reportParser.parse(reportCsv, AreaCode.valueOf(areaCode));
            String summaryPath = null;
            String summaryContent = null;
            if (persistSummary) {
                final SimulationSummaryResult summary = summaryGenerator.generate(
                        new SimulationSummaryRequest(
                                areaCode,
                                intervalStart,
                                intervalEnd,
                                report,
                                reportCsv,
                                Files.isRegularFile(reportTxt) ? reportTxt : null,
                                LocalDateTime.now(),
                                SimulatorFlightDesignatorMap.fromFlights(eligible)));
                summaryPath = summary.summaryPath().toString();
                summaryContent = summary.formattedContent();
            }
            return new SimulationResult(
                    report,
                    eligible.size(),
                    reportCsv.toAbsolutePath().toString(),
                    plansDir.toAbsolutePath().toString(),
                    summaryPath,
                    summaryContent,
                    launch.outputLines());
        } catch (final IOException ex) {
            throw new SimulatorExecutionException("Failed to read simulation report.", ex);
        }
    }

    private static String resolveSimulatorTargetFlightId(final List<Flight> eligible,
                                                         final String targetDesignator) {
        for (final Flight flight : eligible) {
            if (flight.identity().toString().equalsIgnoreCase(targetDesignator)) {
                return Lapr4FlightPlanJson.extractNumericId(flight.flightPlan().jsonContent())
                        .map(String::valueOf)
                        .orElseGet(() -> String.valueOf(SimulatorFlightId.fromDesignator(targetDesignator)));
            }
        }
        return String.valueOf(SimulatorFlightId.fromDesignator(targetDesignator));
    }

    private void exportWeatherSnapshot(final String areaCode,
                                       final List<Flight> eligible,
                                       final LocalDateTime intervalStart,
                                       final LocalDateTime intervalEnd,
                                       final Path plansDir,
                                       final Map<String, String> env) throws IOException {
        if (weatherExporter == null) {
            return;
        }

        List<WeatherData> areaRecords = List.of();
        if (weatherDataRepository != null && intervalStart != null && intervalEnd != null) {
            areaRecords = weatherExporter.resolveAreaRecords(
                    AreaCode.valueOf(areaCode),
                    intervalStart,
                    intervalEnd,
                    weatherDataRepository::findByAreaAndInterval);
        }

        final String weatherJson = weatherExporter.toJson(areaCode, areaRecords, eligible);
        final Path weatherFile = plansDir.resolve("weather_snapshot.json");
        Files.writeString(weatherFile, weatherJson);
        env.put("FS_WEATHER_FILE", weatherFile.toAbsolutePath().toString());
    }

    private String simulatorJsonClippedForArea(final Flight flight, final String areaCode) {
        final AreaCrossingSpan span = eligibilityService.crossingSpanFor(flight, areaCode)
                .orElseThrow(() -> new SimulatorExecutionException(
                        "Flight " + flight.identity() + " does not cross area " + areaCode));
        if (span.isFullLeg()) {
            return exportSimulatorJson(flight);
        }
        final Aircraft aircraft = requireAircraft(flight);
        final FlightPlanDescriptor descriptor = requireDescriptor(flight);
        final FlightPlanDescriptor toExport = span.isFullLeg()
                ? descriptor
                : areaClipper.clip(descriptor, span);
        final Airport[] airports = resolveAirports(toExport, span, areaCode);
        return jsonBuilder.toSimulatorJson(toExport, aircraft, airports[0], airports[1]);
    }

    private Airport[] resolveAirports(final FlightPlanDescriptor descriptor,
                                      final AreaCrossingSpan span,
                                      final String areaCode) {
        final var leg = descriptor.getLegs().get(span.legIndex());
        if (span.isFullLeg()) {
            return new Airport[] {
                    jsonBuilder.requireAirportForCode(leg.getDepartureAirport()),
                    jsonBuilder.requireAirportForCode(leg.getArrivalAirport())
            };
        }
        return new Airport[] {
                "ENT".equals(leg.getDepartureAirport())
                        ? areaClipper.syntheticAirport("ENT", "ZENT", span.entryLatitude(), span.entryLongitude(), areaCode)
                        : jsonBuilder.requireAirportForCode(leg.getDepartureAirport()),
                "EXT".equals(leg.getArrivalAirport())
                        ? areaClipper.syntheticAirport("EXT", "ZEXT", span.exitLatitude(), span.exitLongitude(), areaCode)
                        : jsonBuilder.requireAirportForCode(leg.getArrivalAirport())
        };
    }

    private String simulatorJsonFor(final Flight flight) {
        return exportSimulatorJson(flight);
    }

    private String exportSimulatorJson(final Flight flight) {
        final String stored = flight.flightPlan().jsonContent();
        if (jsonBuilder != null && Lapr4FlightPlanJson.hasSegmentPlan(stored)
                && !Lapr4FlightPlanJson.isSimulatorReady(stored)) {
            return jsonBuilder.enrichLapr4ForSimulator(
                    stored, requireAircraft(flight), requireDescriptor(flight));
        }
        if (jsonBuilder == null || jsonBuilder.isSelfContained(stored)) {
            return stored;
        }
        return jsonBuilder.toSimulatorJson(requireDescriptor(flight), requireAircraft(flight));
    }

    private Aircraft requireAircraft(final Flight flight) {
        if (aircraftRepository == null) {
            throw new SimulatorExecutionException("Aircraft repository is not configured.");
        }
        return aircraftRepository.findByRegistration(
                        AircraftRegistration.valueOf(flight.aircraftRegistration()))
                .orElseThrow(() -> new SimulatorExecutionException(
                        "Aircraft not found for flight " + flight.identity() + ": "
                                + flight.aircraftRegistration()));
    }

    private FlightPlanDescriptor requireDescriptor(final Flight flight) {
        final ParseResult parseResult = dslParser.parse(flight.flightPlan().dslContent());
        if (!parseResult.isValid() || parseResult.getDescriptor() == null) {
            throw new SimulatorExecutionException(
                    "Cannot build simulator JSON for flight " + flight.identity() + ": invalid DSL.");
        }
        return parseResult.getDescriptor();
    }

    private static void validateInterval(final String areaCode,
                                         final LocalDateTime intervalStart,
                                         final LocalDateTime intervalEnd) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (intervalStart == null || intervalEnd == null || intervalEnd.isBefore(intervalStart)) {
            throw new IllegalArgumentException("Invalid simulation interval.");
        }
    }
}
