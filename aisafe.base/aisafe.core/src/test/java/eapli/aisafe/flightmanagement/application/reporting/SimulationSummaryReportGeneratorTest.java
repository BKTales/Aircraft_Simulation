package eapli.aisafe.flightmanagement.application.reporting;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.FlightSimulationReport;
import eapli.aisafe.flightmanagement.domain.simulation.FlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.SafetyViolationEvent;
import eapli.aisafe.flightmanagement.domain.simulation.ScheduleFlightStatus;
import eapli.aisafe.flightmanagement.domain.simulation.ValidationStatus;
import eapli.aisafe.flightmanagement.infrastructure.reporting.SimulationReportsPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationSummaryReportGeneratorTest {

    @Test
    void ensurePassSummaryIsWritten(@TempDir final Path tempDir) throws Exception {
        final SimulationSummaryReportGenerator generator =
                new SimulationSummaryReportGenerator(new SimulationReportsPathResolver(tempDir));
        final LocalDateTime generatedAt = LocalDateTime.of(2026, 6, 8, 14, 30);
        final FlightSimulationReport report = new FlightSimulationReport(
                AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(new FlightStatus("789013", FlightExecutionStatus.SUCCESS)),
                List.of(new ScheduleFlightStatus("789013", ValidationStatus.PASS)),
                List.of());

        final SimulationSummaryResult result = generator.generate(new SimulationSummaryRequest(
                "AREA-0",
                LocalDateTime.of(2026, 5, 26, 0, 0),
                LocalDateTime.of(2026, 8, 26, 23, 59),
                report,
                null,
                null,
                generatedAt));

        assertTrue(Files.isRegularFile(result.summaryPath()));
        assertTrue(result.formattedContent().contains("FINAL RESULT      : PASS"));
        assertTrue(result.formattedContent().contains("789013  SUCCESS"));
        assertTrue(result.formattedContent().contains("SAFETY VIOLATIONS (0)"));
    }

    @Test
    void ensureFailSummaryListsViolations(@TempDir final Path tempDir) {
        final SimulationSummaryReportGenerator generator =
                new SimulationSummaryReportGenerator(new SimulationReportsPathResolver(tempDir));
        final SafetyViolationEvent violation = new SafetyViolationEvent(
                "COLLISION",
                42,
                420,
                "789013",
                "lat 41.12, lon -8.67, alt 9500.00 m",
                "456789",
                "lat 41.13, lon -8.68, alt 9450.00 m");
        final FlightSimulationReport report = new FlightSimulationReport(
                AreaCode.valueOf("AREA-0"),
                ValidationStatus.FAIL,
                List.of(
                        new FlightStatus("789013", FlightExecutionStatus.SUCCESS),
                        new FlightStatus("456789", FlightExecutionStatus.COLLISION)),
                List.of(
                        new ScheduleFlightStatus("789013", ValidationStatus.PASS),
                        new ScheduleFlightStatus("456789", ValidationStatus.FAIL)),
                List.of(violation));

        final SimulationSummaryResult result = generator.generate(new SimulationSummaryRequest(
                "AREA-0",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 18, 0),
                report,
                null,
                null,
                LocalDateTime.of(2026, 6, 8, 15, 0)));

        assertFalse(result.formattedContent().contains("FINAL RESULT      : PASS"));
        assertTrue(result.formattedContent().contains("FINAL RESULT      : FAIL"));
        assertTrue(result.formattedContent().contains("SAFETY VIOLATIONS (1)"));
        assertTrue(result.formattedContent().contains("COLLISION"));
        assertTrue(result.formattedContent().contains("Flight A (789013)"));
    }

    @Test
    void ensureSummaryUsesDesignatorsWhenMapped(@TempDir final Path tempDir) {
        final SimulationSummaryReportGenerator generator =
                new SimulationSummaryReportGenerator(new SimulationReportsPathResolver(tempDir));
        final FlightSimulationReport report = new FlightSimulationReport(
                AreaCode.valueOf("AREA-0"),
                ValidationStatus.PASS,
                List.of(
                        new FlightStatus("653233623", FlightExecutionStatus.LOW_ALTITUDE),
                        new FlightStatus("653233622", FlightExecutionStatus.LOW_ALTITUDE)),
                List.of(),
                List.of());

        final SimulationSummaryResult result = generator.generate(new SimulationSummaryRequest(
                "AREA-0",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 7, 7, 10, 0),
                report,
                null,
                null,
                LocalDateTime.of(2026, 6, 12, 13, 54),
                Map.of("653233623", "TP1001A", "653233622", "TP1001B")));

        assertTrue(result.formattedContent().contains("TP1001A  LOW_ALTITUDE"));
        assertTrue(result.formattedContent().contains("TP1001B  LOW_ALTITUDE"));
    }
}
