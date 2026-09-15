package eapli.aisafe.rcomp.server;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.Optional;

/**
 * Parses TCP LOGIN payload: {@code username;password} or {@code username;password;ATCC|PILOT|WEATHER}.
 */
public final class LoginCredentialsParser {

    public record ParsedLogin(String username, String password, Role requiredRole) {}

    private LoginCredentialsParser() {}

    public static Optional<ParsedLogin> parse(final String credentials) {
        if (credentials == null || credentials.isBlank()) {
            return Optional.empty();
        }
        final String[] parts = credentials.split(";", 3);
        if (parts.length < 2 || parts[0].isBlank()) {
            return Optional.empty();
        }
        final String username = parts[0].trim();
        final String password = parts[1];
        Role role = AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR;
        if (parts.length == 3) {
            final String token = parts[2].trim().toUpperCase();
            role = switch (token) {
                case "ATCC" -> AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR;
                case "PILOT" -> AISafeRoles.PILOT;
                case "WEATHER" -> AISafeRoles.WEATHER_PERSON;
                default -> null;
            };
            if (role == null) {
                return Optional.empty();
            }
        }
        return Optional.of(new ParsedLogin(username, password, role));
    }
}
