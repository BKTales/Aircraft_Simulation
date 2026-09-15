package eapli.aisafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * ISO 3166-1 alpha-2 country code where the aircraft is registered.
 */
@Embeddable
public class RegistrationCountry implements ValueObject {

    @Column(name = "REGISTRATION_COUNTRY", nullable = false, length = 2)
    private String isoAlpha2;

    protected RegistrationCountry() {
        // ORM
    }

    public RegistrationCountry(final String raw) {
        final String normalized = normalize(raw);
        if (!normalized.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Country must be an ISO 3166-1 alpha-2 code.");
        }
        this.isoAlpha2 = normalized;
    }

    public static RegistrationCountry valueOf(final String raw) {
        return new RegistrationCountry(raw);
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Registration country is required.");
        }
        return value.trim().toUpperCase();
    }

    public String isoAlpha2() {
        return isoAlpha2;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegistrationCountry)) {
            return false;
        }
        final RegistrationCountry that = (RegistrationCountry) o;
        return Objects.equals(isoAlpha2, that.isoAlpha2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isoAlpha2);
    }

    @Override
    public String toString() {
        return isoAlpha2;
    }
}
