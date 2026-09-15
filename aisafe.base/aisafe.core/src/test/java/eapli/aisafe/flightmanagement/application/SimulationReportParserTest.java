package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.flightmanagement.domain.simulation.FlightExecutionStatus;
import eapli.aisafe.flightmanagement.domain.simulation.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationReportParserTest {

    private final SimulationReportParser parser = new SimulationReportParser();

    @Test
    void ensureParserReadsValidationResultAndFlights(@TempDir final Path tempDir) throws Exception {
        final Path csv = tempDir.resolve("report.csv");
        Files.writeString(csv, """
                metric,value
                validation_result,PASS

                flight_id,departure,arrival,execution_status,step
                789013,STN,FCO,SUCCESS,15
                """);

        final var report = parser.parse(csv, AreaCode.valueOf("AREA-0"));

        assertTrue(report.passed());
        assertEquals(ValidationStatus.PASS, report.validationResult());
        assertEquals(1, report.flightStatuses().size());
        assertEquals(1, report.scheduleFlightStatuses().size());
        assertTrue(report.safetyViolations().isEmpty());
    }

    @Test
    void ensureLowAltitudeStatusIsParsed(@TempDir final Path tempDir) throws Exception {
        final Path csv = tempDir.resolve("report.csv");
        Files.writeString(csv, """
                metric,value
                validation_result,PASS

                flight_id,departure,arrival,execution_status,step
                789013,STN,FCO,LOW ALTITUDE,8
                """);

        final var report = parser.parse(csv, AreaCode.valueOf("AREA-2"));

        assertEquals(FlightExecutionStatus.LOW_ALTITUDE, report.flightStatuses().get(0).executionStatus());
    }

    @Test
    void ensureFailResultIsParsed(@TempDir final Path tempDir) throws Exception {
        final Path csv = tempDir.resolve("report.csv");
        Files.writeString(csv, """
                metric,value
                validation_result,FAIL
                """);

        final var report = parser.parse(csv, AreaCode.valueOf("AREA-1"));

        assertFalse(report.passed());
    }

    @Test
    void ensureParserReadsViolationsAndIgnoresCollisionSection(@TempDir final Path tempDir) throws Exception {
        final Path csv = tempDir.resolve("report.csv");
        Files.writeString(csv, """
                metric,value
                validation_result,FAIL
                collision_events,1

                collision_flights
                flight_id,departure,arrival,execution_status,step
                111,LPPT,EGLL,COLLISION,42

                flight_id,departure,arrival,execution_status,step,departure_utc,last_update_utc
                789013,LPPT,EGLL,SUCCESS,15,2026-06-01 10:00,2026-06-01 11:00
                456789,LPPT,EGLL,COLLISION,42,2026-06-01 10:30,2026-06-01 11:30

                flight_id_a,flight_id_b,violation_type,step,elapsed_s,separation_m,alt_sep_m,lat_a_deg,lon_a_deg,alt_a_m,speed_kt_a,speed_ms_a,lat_b_deg,lon_b_deg,alt_b_m,speed_kt_b,speed_ms_b
                789013,456789,COLLISION,42,420,150.00,25.00,41.123450,-8.678900,9500.00,450.00,231.48,41.123500,-8.678850,9450.00,445.00,228.91
                """);

        final var report = parser.parse(csv, AreaCode.valueOf("AREA-0"));

        assertFalse(report.passed());
        assertEquals(2, report.flightStatuses().size());
        assertEquals(1, report.safetyViolations().size());
        assertEquals("COLLISION", report.safetyViolations().get(0).violationType());
        assertEquals(42, report.safetyViolations().get(0).step());
        assertEquals(420, report.safetyViolations().get(0).elapsedSeconds());
        assertEquals("789013", report.safetyViolations().get(0).flightIdA());
        assertEquals("456789", report.safetyViolations().get(0).flightIdB());
    }
}
