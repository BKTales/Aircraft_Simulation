package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.ValueObject;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional one-letter operational suffix for a flight designator (xxnnnn(a)).
 */
public final class OperationalSuffix implements ValueObject {

    private final String letter;

    private OperationalSuffix(final String letter) {
        this.letter = letter;
    }

    public static Optional<OperationalSuffix> optionalOf(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        final String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 1 || !Character.isLetter(normalized.charAt(0))) {
            throw new IllegalArgumentException("Operational suffix must be a single letter.");
        }
        return Optional.of(new OperationalSuffix(normalized));
    }

    public String letter() {
        return letter;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationalSuffix that)) {
            return false;
        }
        return Objects.equals(letter, that.letter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter);
    }

    @Override
    public String toString() {
        return letter;
    }
}
