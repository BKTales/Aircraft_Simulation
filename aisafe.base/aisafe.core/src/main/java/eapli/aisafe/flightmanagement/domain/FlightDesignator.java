package eapli.aisafe.flightmanagement.domain;

import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.Optional;

@Embeddable
public class FlightDesignator implements ValueObject, Comparable<FlightDesignator>{
    private String code;

    protected FlightDesignator() {
        // ORM
    }

    public FlightDesignator(final String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Designator code cannot be empty");
        }
        this.code = code;
    }

    public static FlightDesignator fromRoute(final RouteName route,
                                             final Optional<OperationalSuffix> suffix) {
        if (route == null) {
            throw new IllegalArgumentException("Route name is required.");
        }
        final String base = route.toString();
        if (suffix == null || suffix.isEmpty()) {
            return new FlightDesignator(base);
        }
        return new FlightDesignator(base + suffix.get().letter());
    }

    @Override
    public int compareTo(final FlightDesignator o) {
        if (this.code == null) {
            return o.code == null ? 0 : -1;
        }
        if (o.code == null) {
            return 1;
        }
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FlightDesignator that = (FlightDesignator) o;
        return Objects.equals(this.code, that.code);
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