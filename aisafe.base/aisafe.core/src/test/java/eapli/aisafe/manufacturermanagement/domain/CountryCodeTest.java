package eapli.aisafe.manufacturermanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class CountryCodeTest {

    @Test
    void ensureCountryCodeIsCreatedAndReturnsValue() {
        String expected = "PT";
        CountryCode subject = new CountryCode(expected);
        assertEquals(expected, subject.code());
    }

    @Test
    void ensureNullCountryCodeThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CountryCode(null));
        assertEquals("Country code cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureEmptyCountryCodeThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CountryCode(""));
        assertEquals("Country code cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureBlankCountryCodeThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CountryCode("   "));
        assertEquals("Country code cannot be empty.", exception.getMessage());
    }

    @Test
    void ensureEqualsAndHashCodeWorksAsExpected() {

        CountryCode pt1 = new CountryCode("PT");
        CountryCode pt2 = new CountryCode("PT");
        CountryCode us = new CountryCode("US");


        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
        assertEquals(pt1, pt1);
        assertNotEquals(pt1, us);
        assertNotEquals(null, pt1);
        assertNotEquals("PT", pt1);
    }

    @Test
    void ensureProtectedConstructorExistsForORM() throws Exception {

        Constructor<CountryCode> constructor = CountryCode.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CountryCode instance = constructor.newInstance();


        assertNotNull(instance);
        assertNull(instance.code());
    }
}