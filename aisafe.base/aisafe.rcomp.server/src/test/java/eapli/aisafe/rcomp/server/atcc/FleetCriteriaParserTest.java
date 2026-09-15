package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.aircraftmanagement.application.FleetListCriteria;
import eapli.aisafe.aircraftmanagement.application.FleetNumericComparison;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetCriteriaParserTest {

    @Test
    void unfiltered() {
        assertTrue(FleetCriteriaParser.parse("UNFILTERED").isUnfiltered());
        assertTrue(FleetCriteriaParser.parse("").isUnfiltered());
    }

    @Test
    void byModel() {
        final FleetListCriteria c = FleetCriteriaParser.parse("MODEL;A320");
        assertEquals(AircraftModelId.valueOf("A320"), c.modelId().orElseThrow());
    }

    @Test
    void byManufacturer() {
        final FleetListCriteria c = FleetCriteriaParser.parse("MANUFACTURER;MAN01");
        assertEquals(ManufacturerId.valueOf("MAN01"), c.manufacturerId().orElseThrow());
    }

    @Test
    void byPassengers() {
        final FleetListCriteria c = FleetCriteriaParser.parse("PASSENGERS;180;GT");
        assertEquals(180, c.passengerCapacity().orElseThrow());
        assertEquals(FleetNumericComparison.GREATER_THAN, c.passengerCapacityComparison().orElseThrow());
    }

    @Test
    void byAge() {
        final FleetListCriteria c = FleetCriteriaParser.parse("AGE;10;EQ");
        assertEquals(10, c.ageInYears().orElseThrow());
        assertEquals(FleetNumericComparison.EQUAL, c.ageComparison().orElseThrow());
    }
}
