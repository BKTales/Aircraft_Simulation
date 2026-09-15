package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.aisafe.routemanagement.application.ActiveRouteOption;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.framework.visitor.Visitor;

import java.time.LocalDate;

/**
 * US074 route picker table. Header is indented to align with {@code SelectWidget} option prefix ({@code 1. }).
 */
@SuppressWarnings("squid:S106")
public final class DeactivateRouteListPrinter implements Visitor<ActiveRouteOption> {

    private static final int SELECT_INDEX_PREFIX_WIDTH = 4;

    private static final String ROW_FORMAT = "%-12s  %-8s  %-8s  %-10s  %-22s";

    private static final String HEADER_FORMAT = "%" + SELECT_INDEX_PREFIX_WIDTH + "s" + ROW_FORMAT;

    public static final int TABLE_PRINT_WIDTH =
            SELECT_INDEX_PREFIX_WIDTH + 12 + 2 + 8 + 2 + 8 + 2 + 10 + 2 + 22;

    public static void printTableHeaderWithRule() {
        System.out.printf(
                HEADER_FORMAT + "%n",
                "",
                "Route Name",
                "Origin",
                "Destination",
                "Route Type",
                "Last Planned Flight");
        System.out.println("-".repeat(TABLE_PRINT_WIDTH));
    }

    @Override
    public void visit(final ActiveRouteOption option) {
        final Route route = option.route();
        final String lastFlight = option.lastPlannedFlightDeparture()
                .map(LocalDate::toString)
                .orElse("none");
        System.out.printf(
                ROW_FORMAT + "%n",
                route.identity(),
                route.originAirportIATACode(),
                route.destinationAirportIATACode(),
                route.flightType(),
                lastFlight);
    }
}
