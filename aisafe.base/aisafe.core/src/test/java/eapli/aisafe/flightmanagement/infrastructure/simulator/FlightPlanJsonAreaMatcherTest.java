package eapli.aisafe.flightmanagement.infrastructure.simulator;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightPlanJsonAreaMatcherTest {

    @Test
    void extractFirstAreaCodeReturnsValue() {
        final Optional<String> area = FlightPlanJsonAreaMatcher.extractFirstAreaCode(
                "{\"Leg\":[{\"Departure\":{\"AreaCode\":\"AREA-3\"}}]}");
        assertTrue(area.isPresent());
        assertEquals("AREA-3", area.get());
    }

    @Test
    void extractFirstAreaCodeEmptyWhenMissing() {
        assertTrue(FlightPlanJsonAreaMatcher.extractFirstAreaCode("{\"ID\":1}").isEmpty());
        assertTrue(FlightPlanJsonAreaMatcher.extractFirstAreaCode(null).isEmpty());
    }

    @Test
    void matchesAreaIgnoresCase() {
        assertTrue(FlightPlanJsonAreaMatcher.matchesArea(
                "{\"AreaCode\":\"area-1\"}", "AREA-1"));
        assertFalse(FlightPlanJsonAreaMatcher.matchesArea(
                "{\"AreaCode\":\"AREA-2\"}", "AREA-1"));
    }
}
