package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuelTypeTest {

    @Test
    void ensureFuelTypeEnumLoadsAndValueOfWorks() {
        assertTrue(FuelType.values().length >= 1);
        assertSame(FuelType.JET_A1, FuelType.valueOf("JET_A1"));
        assertSame(FuelType.ELECTRICITY, FuelType.valueOf("ELECTRICITY"));
    }
}

