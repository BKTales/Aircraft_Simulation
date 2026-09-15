package eapli.aisafe.rcomp.server;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginCredentialsParserTest {

    @Test
    void parseTwoFieldsDefaultsToAtcc() {
        final var parsed = LoginCredentialsParser.parse("user1;password123").orElseThrow();
        assertEquals("user1", parsed.username());
        assertEquals("password123", parsed.password());
        assertEquals(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, parsed.requiredRole());
    }

    @Test
    void parseExplicitAtcc() {
        final var parsed = LoginCredentialsParser.parse("user1;password123;ATCC").orElseThrow();
        assertEquals(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, parsed.requiredRole());
    }

    @Test
    void parseExplicitPilot() {
        final var parsed = LoginCredentialsParser.parse("pilot1;password123;PILOT").orElseThrow();
        assertEquals("pilot1", parsed.username());
        assertEquals(AISafeRoles.PILOT, parsed.requiredRole());
    }

    @Test
    void parseExplicitWeather() {
        final var parsed = LoginCredentialsParser.parse("weather;password123;WEATHER").orElseThrow();
        assertEquals("weather", parsed.username());
        assertEquals(AISafeRoles.WEATHER_PERSON, parsed.requiredRole());
    }

    @Test
    void parseRejectsUnknownRoleToken() {
        assertTrue(LoginCredentialsParser.parse("user;pass;ADMIN").isEmpty());
    }

    @Test
    void parseRejectsMissingPassword() {
        assertTrue(LoginCredentialsParser.parse("user").isEmpty());
    }
}
