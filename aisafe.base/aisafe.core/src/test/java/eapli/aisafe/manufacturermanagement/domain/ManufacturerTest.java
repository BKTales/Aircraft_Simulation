package eapli.aisafe.manufacturermanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ManufacturerTest {

    private Manufacturer createSubject(String id) {
        return new Manufacturer(
                ManufacturerId.valueOf(id),
                new ManufacturerName("Boeing"),
                new CountryCode("US")
        );
    }

    @Test
    void ensureManufacturerIsCreatedCorrectly() {
        // Arrange & Act
        Manufacturer subject = createSubject("BOE");

        // Assert
        assertNotNull(subject);
        assertEquals(ManufacturerId.valueOf("BOE"), subject.identity());
        assertEquals(new ManufacturerName("Boeing"), subject.name());
        assertEquals(new CountryCode("US"), subject.countryCode());
    }

    @Test
    void ensureSameAsWorksForIdenticalIdentity() {
        Manufacturer a = createSubject("BOE");
        Manufacturer b = createSubject("BOE");

        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAsFailsForDifferentIdentity() {
        Manufacturer a = createSubject("BOE");
        Manufacturer c = createSubject("AIR");

        assertFalse(a.sameAs(c));
    }

    @Test
    void ensureSameAsHandlesOtherTypesAndNull() {
        Manufacturer a = createSubject("BOE");

        assertThrows(ClassCastException.class, () -> a.sameAs(new Object()));
        assertThrows(NullPointerException.class, () -> a.sameAs(null));
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {
        Constructor<Manufacturer> constructor = Manufacturer.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Manufacturer instance = constructor.newInstance();

        assertNotNull(instance);
        assertNull(instance.identity());
        assertNull(instance.name());
        assertNull(instance.countryCode());
    }
}