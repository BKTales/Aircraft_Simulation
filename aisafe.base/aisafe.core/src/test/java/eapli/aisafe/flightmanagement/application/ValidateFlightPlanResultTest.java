package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateFlightPlanResultTest {

    @Test
    void approvedFactoryProducesPassingApprovedResult() {
        final ValidateFlightPlanResult result =
                ValidateFlightPlanResult.approved("TP123", List.of("[INIT] ok"));

        assertTrue(result.passed());
        assertEquals(FlightPlanStatus.SIM_APPROVED, result.status());
        assertFalse(result.dslFailure());
        assertEquals(List.of("[INIT] ok"), result.simulationLog());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void rejectedFactoryProducesFailingRejectedResult() {
        final ValidateFlightPlanResult result =
                ValidateFlightPlanResult.rejected("TP123", "boom", List.of("line"));

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.SIM_REJECTED, result.status());
        assertFalse(result.dslFailure());
        assertEquals("boom", result.message());
        assertEquals(List.of("line"), result.simulationLog());
    }

    @Test
    void blockedFactoryStaysInDraftWithoutErrors() {
        final ValidateFlightPlanResult result =
                ValidateFlightPlanResult.blocked("TP123", "Flight has no schedule.");

        assertFalse(result.passed());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertFalse(result.dslFailure());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void dslFailureFactoryFlagsDslFailureAndKeepsContent() {
        final ValidateFlightPlanResult result = ValidateFlightPlanResult.dslFailure(
                "TP123", List.of("line 1: error"), "flight TP123 {}");

        assertFalse(result.passed());
        assertTrue(result.dslFailure());
        assertEquals(FlightPlanStatus.DRAFT, result.status());
        assertEquals("flight TP123 {}", result.dslContent());
        assertEquals(List.of("line 1: error"), result.errors());
    }

    @Test
    void dslFailureIsFalseWhenErrorsEmptyEvenInDraft() {
        final ValidateFlightPlanResult result =
                ValidateFlightPlanResult.blocked("TP123", "blocked");

        assertFalse(result.dslFailure());
    }

    @Test
    void nullCollectionsAreNormalisedToEmptyImmutableLists() {
        final ValidateFlightPlanResult result = new ValidateFlightPlanResult(
                false, "TP123", FlightPlanStatus.DRAFT, "msg", null, null, null);

        assertTrue(result.errors().isEmpty());
        assertTrue(result.simulationLog().isEmpty());
    }
}
