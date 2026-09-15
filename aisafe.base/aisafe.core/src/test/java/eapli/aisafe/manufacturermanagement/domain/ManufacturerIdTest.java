package eapli.aisafe.manufacturermanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ManufacturerIdTest {

    @Test
    void ensureManufacturerIdIsCreatedWithValidCode() {

        final String code = "AIR";
        final ManufacturerId subject = ManufacturerId.valueOf(code);

                assertEquals(code, subject.toString());
    }

    @Test
    void ensureManufacturerIdConvertsToUpperCase() {
        final ManufacturerId subject = ManufacturerId.valueOf("airbus");

                assertEquals("AIRBUS", subject.toString());
    }

    @Test
    void ensureConstructorThrowsExceptionForNullInput() {
        assertThrows(IllegalArgumentException.class, () -> ManufacturerId.valueOf(null));
    }

    @Test
    void ensureConstructorThrowsExceptionForEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> ManufacturerId.valueOf(""));
    }

    @Test
    void ensureConstructorThrowsExceptionForBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> ManufacturerId.valueOf("   "));
    }

    @Test
    void ensureCompareToWorks() {

        ManufacturerId a = ManufacturerId.valueOf("AIRBUS");
        ManufacturerId b = ManufacturerId.valueOf("BOEING");

                assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(ManufacturerId.valueOf("airbus")));
    }

    @Test
    void ensureEqualsAndHashCodeCoverAllLines() {

        ManufacturerId id1 = ManufacturerId.valueOf("AIR");
        ManufacturerId id2 = ManufacturerId.valueOf("air");
        ManufacturerId id3 = ManufacturerId.valueOf("BOE");


        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(id1, id1);
        assertNotEquals(id1, id3);
        assertNotEquals(null, id1);
        assertNotEquals("AIR", id1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
             Constructor<ManufacturerId> constructor = ManufacturerId.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ManufacturerId instance = constructor.newInstance();

                assertNotNull(instance);
        assertNull(instance.toString());
    }
}