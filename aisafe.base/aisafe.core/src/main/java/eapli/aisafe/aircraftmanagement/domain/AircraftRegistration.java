package eapli.aisafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Aircraft tail registration (globally unique identifier for the physical aircraft).
 */
@Embeddable
public class AircraftRegistration implements ValueObject, Comparable<AircraftRegistration> {

    @Column(name = "REGISTRATION", nullable = false, unique = true, length = 14)
    private String code;

    protected AircraftRegistration() {
        // ORM
    }

    public AircraftRegistration(final String raw) {
        final String normalized = normalize(raw);
        if (!normalized.matches("^[A-Z0-9-]{3,14}$")) {
            throw new IllegalArgumentException(
                    "Registration must be 3–14 characters (letters, digits, hyphen).");
        }
        this.code = normalized;
    }

    public static AircraftRegistration valueOf(final String raw) {
        return new AircraftRegistration(raw);
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Registration is required.");
        }
        return value.trim().toUpperCase().replaceAll("\\s+", "");
    }

    @Override
    public int compareTo(final AircraftRegistration o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AircraftRegistration)) {
            return false;
        }
        final AircraftRegistration that = (AircraftRegistration) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
