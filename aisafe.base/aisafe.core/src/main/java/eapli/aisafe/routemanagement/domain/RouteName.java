package eapli.aisafe.routemanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class RouteName implements ValueObject, Comparable<RouteName> {

    @Column(name = "ROUTE_NAME", nullable = false, unique = true, length = 6)
    private String value;

    protected RouteName() {
        // for ORM
    }

    private RouteName(final String value) {
        final String normalized = normalize(value);
        if (!normalized.matches("^[A-Z]{2}[0-9]{1,4}$")) {
            throw new IllegalArgumentException("Route name must be 2 letters followed by 1 to 4 digits.");
        }
        this.value = normalized;
    }

    public static RouteName valueOf(final String value) {
        return new RouteName(value);
    }

    private static String normalize(final String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase();
    }

    @Override
    public int compareTo(final RouteName other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RouteName that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
