package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightPlanTest {

    private static final String JSON = "{\"ID\":1,\"Type\":\"regular\",\"Leg\":[]}";
    private static final FlightDesignator DESIGNATOR = new FlightDesignator("TP1001");

    @Test
    void ensureFlightPlanIsCreatedWithValidData() {
        final FlightPlanStatus status = FlightPlanStatus.DRAFT;
        final FuelLoad fuel = new FuelLoad(5000.0);

        final FlightPlan subject = FlightPlan.forFlight(DESIGNATOR, status, fuel, JSON);

        assertEquals(FlightPlanId.of(DESIGNATOR), subject.identity());
        assertEquals(status, subject.status());
        assertEquals(fuel, subject.fuelLoad());
        assertEquals(JSON, subject.jsonContent());
    }

    @Test
    void ensureIdentityEquality() {
        final FlightPlan p1 = FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON);
        final FlightPlan p2 = FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.SUBMITTED_FOR_SIMULATION,
                new FuelLoad(2000.0), "{\"ID\":2}");

        assertTrue(p1.sameAs(p2));
        assertEquals(p1.identity(), p2.identity());
    }

    @Test
    void ensureDifferentFlightCodesAreNotEqual() {
        final FlightPlan p1 = FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON);
        final FlightPlan pOther = FlightPlan.forFlight(new FlightDesignator("TP2000"),
                FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON);

        assertNotEquals(p1, pOther);
    }

    @Test
    void ensureChangeStatusUpdatesField() {
        final FlightPlan plan = FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON);
        plan.changeStatus(FlightPlanStatus.SIM_APPROVED);
        assertEquals(FlightPlanStatus.SIM_APPROVED, plan.status());
    }

    @Test
    void ensureFactoriesRejectInvalidMandatoryData() {
        assertThrows(NullPointerException.class,
                () -> FlightPlan.forFlight(null, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlan.forFlight(DESIGNATOR, null, new FuelLoad(1000.0), JSON));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, null, JSON));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), " "));
    }

    @Test
    void ensureAssignFlightPlanRejectsMismatchedIdentity() {
        final Flight flight = new Flight(DESIGNATOR, "OPO-LIS", "CS-DEMO");
        final FlightPlan wrongPlan = FlightPlan.forFlight(new FlightDesignator("TP9999"),
                FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON);

        assertThrows(IllegalArgumentException.class, () -> flight.assignFlightPlan(wrongPlan));
    }

    @Test
    void ensureTransitionFlightPlanStatusOnFlight() {
        final Flight flight = new Flight(DESIGNATOR, "OPO-LIS", "CS-DEMO");
        flight.assignFlightPlan(FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT, new FuelLoad(1000.0), JSON));

        flight.transitionFlightPlanStatus(FlightPlanStatus.SUBMITTED_FOR_SIMULATION);

        assertEquals(FlightPlanStatus.SUBMITTED_FOR_SIMULATION, flight.flightPlan().status());
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        final Constructor<FlightPlan> constructor = FlightPlan.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final FlightPlan instance = constructor.newInstance();

        assertNotNull(instance);
        assertNull(instance.status());
        assertNull(instance.fuelLoad());
    }

    @Test
    void ensureHasDslAndJsonContent() {
        final FlightPlan withDsl = FlightPlan.forFlight(DESIGNATOR, FlightPlanStatus.DRAFT,
                new FuelLoad(1000.0), "flight TP1001;", JSON);
        assertTrue(withDsl.hasDslContent());
        assertTrue(withDsl.hasJsonContent());
    }
}
