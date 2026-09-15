package eapli.aisafe.flightmanagement.infrastructure.reporting;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationReportDesignatorRewriterTest {

    private static final Map<String, String> MAP = Map.of(
            "653233623", "TP1001A",
            "653233622", "TP1001B");

    @Test
    void ensureCsvFlightRowsUseDesignators() {
        final String csv = """
                metric,value
                validation_result,PASS

                flight_id,departure,arrival,execution_status,step
                653233623,ENT,LIS,LOW ALTITUDE,1
                653233622,ENT,LIS,LOW ALTITUDE,1
                """;

        final String rewritten = SimulationReportDesignatorRewriter.rewriteCsv(csv, MAP);

        assertTrue(rewritten.contains("TP1001A,ENT,LIS,LOW ALTITUDE,1"));
        assertTrue(rewritten.contains("TP1001B,ENT,LIS,LOW ALTITUDE,1"));
    }

    @Test
    void ensureTxtFlightIdsUseDesignators() {
        final String txt = """
                [Flight 000]  Flight ID 653233623
                  Flight A (ID 653233623)
                  Flight B (ID 653233622)
                """;

        final String rewritten = SimulationReportDesignatorRewriter.rewriteTxt(txt, MAP);

        assertEquals("""
                [Flight 000]  Flight ID TP1001A
                  Flight A (ID TP1001A)
                  Flight B (ID TP1001B)
                """, rewritten);
    }
}
