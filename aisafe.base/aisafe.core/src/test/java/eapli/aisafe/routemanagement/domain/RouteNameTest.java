package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteNameTest {

    @Test
    void acceptsValidName() {
        assertEquals("TP123", RouteName.valueOf("TP123").toString());
    }

    @Test
    void normalizesToUppercaseAndTrim() {
        assertEquals("TP1", RouteName.valueOf(" tp1 ").toString());
    }

    @Test
    void rejectsInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("T123"));
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TP12345"));
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TPAB"));
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf(null));
    }

    @Test
    void compareToOrdersLexicographically() {
        assertTrue(RouteName.valueOf("TP1").compareTo(RouteName.valueOf("TP2")) < 0);
    }

    @Test
    void equalsAndHashCode() {
        final RouteName a = RouteName.valueOf("FR2001");
        final RouteName b = RouteName.valueOf("fr2001");
        final RouteName c = RouteName.valueOf("FR2002");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
