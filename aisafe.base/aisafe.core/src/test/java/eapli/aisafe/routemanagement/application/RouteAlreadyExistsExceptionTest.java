package eapli.aisafe.routemanagement.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RouteAlreadyExistsExceptionTest {

    @Test
    void carriesMessage() {
        final RouteAlreadyExistsException ex = new RouteAlreadyExistsException("Route name already exists.");
        assertEquals("Route name already exists.", ex.getMessage());
        assertInstanceOf(IllegalArgumentException.class, ex);
    }
}
