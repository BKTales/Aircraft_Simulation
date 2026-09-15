package eapli.aisafe.enginemodelmanagement.domain;

import java.util.Locale;
import java.util.Map;

public enum MotorizationType {
    TURBOPROP("turboprop"),
    TURBOFAN("turbofan"),
    TURBOJET("turbojet"),
    RAMJET("ramjet"),
    ELECTRIC_PROPELLER("electric propeller");

    private static final Map<String, MotorizationType> BY_LABEL = Map.of(
            "turboprop", TURBOPROP,
            "turbofan", TURBOFAN,
            "turbojet", TURBOJET,
            "ramjet", RAMJET,
            "electric propeller", ELECTRIC_PROPELLER
    );

    private final String label;

    MotorizationType(final String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static MotorizationType fromLabel(final String label) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Motorization type cannot be empty.");
        }
        final var mt = BY_LABEL.get(label.trim().toLowerCase(Locale.ROOT));
        if (mt == null) {
            throw new IllegalArgumentException("Motorization type must be one of: turboprop, turbofan, turbojet, ramjet, electric propeller");
        }
        return mt;
    }
}

