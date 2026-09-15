package eapli.aisafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneTest {

    @Test
    void shouldCreateValidPhoneNumber() {
        Phone phone = new Phone("+351912345678");
        assertEquals("+351912345678", phone.toString());
    }

    @Test
    void shouldAcceptAny9DigitsAfter351() {
        Phone phone = new Phone("+351123456789");
        assertEquals("+351123456789", phone.toString());
    }

    @Test
    void shouldFailWhenNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone(null);
        });
    }

    @Test
    void shouldFailWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone("");
        });
    }

    @Test
    void shouldFailWhenMissingPlus() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone("351912345678");
        });
    }

    @Test
    void shouldFailWhenTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone("+35191234567"); // 8 dígitos
        });
    }

    @Test
    void shouldFailWhenTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone("+3519123456789"); // 10 dígitos
        });
    }

    @Test
    void shouldFailWhenContainsLetters() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Phone("+35191A345678");
        });
    }

    @Test
    void shouldBeEqualWhenSameNumber() {
        Phone p1 = new Phone("+351912345678");
        Phone p2 = new Phone("+351912345678");

        assertEquals(p1, p2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentNumber() {
        Phone p1 = new Phone("+351912345678");
        Phone p2 = new Phone("+351923456789");

        assertNotEquals(p1, p2);
    }

    @Test
    void shouldCompareCorrectly() {
        Phone p1 = new Phone("+351912345678");
        Phone p2 = new Phone("+351923456789");

        assertTrue(p1.compareTo(p2) < 0);
    }
}