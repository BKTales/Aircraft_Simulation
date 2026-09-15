package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeactivationDateTest {

    @Test
    void valueOfCreatesDate() {
        final LocalDate date = LocalDate.of(2026, 12, 31);
        final DeactivationDate deactivation = DeactivationDate.valueOf(date);
        assertEquals(date, deactivation.value());
        assertEquals("2026-12-31", deactivation.toString());
    }

    @Test
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new DeactivationDate(null));
    }

    @Test
    void equalsAndHashCode() {
        final DeactivationDate a = DeactivationDate.valueOf(LocalDate.of(2026, 1, 1));
        final DeactivationDate b = DeactivationDate.valueOf(LocalDate.of(2026, 1, 1));
        final DeactivationDate c = DeactivationDate.valueOf(LocalDate.of(2026, 2, 1));

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "2026-01-01");
    }
}
