package eapli.aisafe.flightmanagement.domain;

import eapli.aisafe.routemanagement.domain.RouteName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalSuffixTest {

    @Test
    void fromRouteWithoutSuffix() {
        assertEquals("TP123", FlightDesignator.fromRoute(RouteName.valueOf("TP123"), Optional.empty()).toString());
    }

    @Test
    void fromRouteWithSuffix() {
        final Optional<OperationalSuffix> suffix = OperationalSuffix.optionalOf("a");
        assertEquals("TP123A", FlightDesignator.fromRoute(RouteName.valueOf("TP123"), suffix).toString());
    }

    @Test
    void rejectsInvalidSuffix() {
        assertThrows(IllegalArgumentException.class, () -> OperationalSuffix.optionalOf("12"));
    }

    @Test
    void emptyOptionalWhenBlank() {
        assertTrue(OperationalSuffix.optionalOf("").isEmpty());
    }
}
