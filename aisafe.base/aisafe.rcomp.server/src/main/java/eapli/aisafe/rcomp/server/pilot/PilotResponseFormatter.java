package eapli.aisafe.rcomp.server.pilot;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.routemanagement.domain.Route;

public final class PilotResponseFormatter {

    private PilotResponseFormatter() {}

    public static String formatRoute(final Route route) {
        return String.join("|",
                route.identity().toString(),
                route.originAirport().identity().toString(),
                route.destinationAirport().identity().toString(),
                route.flightType().name());
    }

    public static String formatPilot(final PilotUser pilot) {
        return String.join("|",
                pilot.systemUser().username().toString(),
                pilot.systemUser().name().firstName(),
                pilot.systemUser().name().lastName(),
                pilot.systemUser().email().toString());
    }

    public static String formatFlight(final Flight flight) {
        final String status = flight.flightPlan() == null
                ? "NO_PLAN"
                : flight.flightPlan().status().name();
        final String weather = flight.weatherData() != null
                ? String.valueOf(flight.weatherData().identity())
                : "";
        return String.join("|",
                flight.identity().toString(),
                flight.routeName() != null ? flight.routeName() : "",
                flight.aircraftRegistration() != null ? flight.aircraftRegistration() : "",
                status,
                weather);
    }

    public static String joinLines(final Iterable<String> lines) {
        final StringBuilder sb = new StringBuilder();
        for (final String line : lines) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
