package eapli.aisafe.routemanagement.application;

import eapli.aisafe.routemanagement.domain.Route;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Active route shown in the deactivate-route UI, with optional last planned flight departure date.
 */
public final class ActiveRouteOption {

    private final Route route;
    private final Optional<LocalDate> lastPlannedFlightDeparture;

    public ActiveRouteOption(final Route route, final Optional<LocalDate> lastPlannedFlightDeparture) {
        this.route = Objects.requireNonNull(route, "route");
        this.lastPlannedFlightDeparture = Objects.requireNonNull(lastPlannedFlightDeparture, "lastPlannedFlightDeparture");
    }

    public Route route() {
        return route;
    }

    public Optional<LocalDate> lastPlannedFlightDeparture() {
        return lastPlannedFlightDeparture;
    }
}
