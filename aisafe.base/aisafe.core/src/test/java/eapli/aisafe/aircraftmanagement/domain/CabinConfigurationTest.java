package eapli.aisafe.aircraftmanagement.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfSeats;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CabinConfigurationTest {

    @Test
    void protectedNoArgConstructorExistsForOrm() {
        assertNotNull(new CabinConfiguration());
    }

    @Test
    void ofEconomyBusinessFirstBuildsAndTotalSeats() {
        final CabinConfiguration c = CabinConfiguration.ofEconomyBusinessFirst(10, 5, 2);
        assertEquals(10, c.economySeats().seats());
        assertEquals(5, c.businessSeats().seats());
        assertEquals(2, c.firstClassSeats().seats());
        assertEquals(17, c.totalSeats());
    }

    @Test
    void constructorRejectsNullSeatComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> new CabinConfiguration(null, NumberOfSeats.valueOf(1), NumberOfSeats.valueOf(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new CabinConfiguration(NumberOfSeats.valueOf(1), null, NumberOfSeats.valueOf(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new CabinConfiguration(NumberOfSeats.valueOf(1), NumberOfSeats.valueOf(0), null));
    }

    @Test
    void identityIsNullUntilPersisted() {
        assertNull(CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0).identity());
    }

    @Test
    void sameAsDelegatesToEquals() {
        final CabinConfiguration a = CabinConfiguration.ofEconomyBusinessFirst(3, 1, 0);
        final CabinConfiguration b = CabinConfiguration.ofEconomyBusinessFirst(3, 1, 0);
        assertTrue(a.sameAs(b));
        assertFalse(a.sameAs(CabinConfiguration.ofEconomyBusinessFirst(2, 1, 0)));
    }

    @Test
    void equalsUsesIdWhenBothPresent() throws Exception {
        final CabinConfiguration c1 = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        final CabinConfiguration c2 = CabinConfiguration.ofEconomyBusinessFirst(9, 9, 9);
        setId(c1, 42L);
        setId(c2, 42L);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void equalsUsesSeatBreakdownWhenIdsNull() {
        final CabinConfiguration a = CabinConfiguration.ofEconomyBusinessFirst(4, 0, 1);
        final CabinConfiguration b = CabinConfiguration.ofEconomyBusinessFirst(4, 0, 1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsReflexiveAndRejectsOtherType() {
        final CabinConfiguration c = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        assertEquals(c, c);
        assertNotEquals(c, "not-a-cabin");
    }

    @Test
    void equalsFalseWhenBothIdsNonNullButDifferent() throws Exception {
        final CabinConfiguration c1 = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        final CabinConfiguration c2 = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        setId(c1, 1L);
        setId(c2, 2L);
        assertNotEquals(c1, c2);
    }

    @Test
    void equalsUsesSeatBreakdownWhenOnlyOneSideHasId() throws Exception {
        final CabinConfiguration withId = CabinConfiguration.ofEconomyBusinessFirst(2, 1, 0);
        final CabinConfiguration withoutId = CabinConfiguration.ofEconomyBusinessFirst(2, 1, 0);
        setId(withId, 99L);
        assertEquals(withId, withoutId);
        assertEquals(withoutId, withId);
    }

    @Test
    void equalsFalseWhenIdsNullButSeatsDiffer() {
        final CabinConfiguration a = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        final CabinConfiguration b = CabinConfiguration.ofEconomyBusinessFirst(2, 0, 0);
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    private static void setId(final CabinConfiguration c, final Long id) throws Exception {
        final Field f = CabinConfiguration.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(c, id);
    }
}
