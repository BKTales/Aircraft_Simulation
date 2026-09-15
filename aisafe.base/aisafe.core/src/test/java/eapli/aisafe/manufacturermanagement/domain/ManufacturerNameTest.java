package eapli.aisafe.manufacturermanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ManufacturerNameTest {

    @Test
    void ensureManufacturerNameIsCreatedWithValidString() {
        final String name = "Airbus";
        final ManufacturerName subject = new ManufacturerName(name);

        // Assert
        assertEquals(name, subject.toString());
    }

    @Test
    void ensureConstructorThrowsExceptionForNullInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ManufacturerName(null));
        assertEquals("Manufacturer Name cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureConstructorThrowsExceptionForEmptyInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ManufacturerName(""));
        assertEquals("Manufacturer Name cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureConstructorThrowsExceptionForBlankInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ManufacturerName("   "));
        assertEquals("Manufacturer Name cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureEqualsAndHashCodeCoverAllLines() {
        ManufacturerName n1 = new ManufacturerName("Airbus");
        ManufacturerName n2 = new ManufacturerName("Airbus");
        ManufacturerName n3 = new ManufacturerName("Boeing");

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
        assertEquals(n1, n1);
        assertNotEquals(n1, n3);
        assertNotEquals(n1.hashCode(), n3.hashCode());
        assertNotEquals(null, n1);
        assertNotEquals("Airbus", n1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<ManufacturerName> constructor = ManufacturerName.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ManufacturerName instance = constructor.newInstance();

        assertNotNull(instance);
        assertNull(instance.toString());
    }
}