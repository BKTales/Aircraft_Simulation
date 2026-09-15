package eapli.aisafe.flightmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FuelQuantityTest {

    @Test
    void convertsLitresToKg() {
        final FuelQuantity fuel = new FuelQuantity(1000, "l");
        assertEquals(804.0, fuel.toKg(), 0.01);
    }

    @Test
    void keepsKgAsKg() {
        final FuelQuantity fuel = FuelQuantity.kilograms(4500);
        assertEquals(4500, fuel.toKg());
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(0, "kg"));
    }
}
