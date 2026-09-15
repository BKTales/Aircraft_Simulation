package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.routemanagement.domain.RecurringScheduleEntry;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteRecurringSchedule;
import eapli.aisafe.routemanagement.domain.RouteSchedule;

import java.util.stream.Collectors;

@SuppressWarnings("squid:S106")
public final class RoutePrinter {

    private static final String SEPARATOR = "----------------------------------------";

    private RoutePrinter() {
        // utility
    }

    public static void printCreationSummary(final Route route) {
        System.out.println();
        System.out.println("Route created successfully.");
        System.out.println(SEPARATOR);
        System.out.printf("Route name           : %s%n", route.identity());
        System.out.printf("Company IATA code    : %s%n", route.companyIATACode());
        System.out.printf("Origin airport       : %s%n", route.originAirportIATACode());
        System.out.printf("Destination airport  : %s%n", route.destinationAirportIATACode());
        System.out.printf("Flight type          : %s%n", route.flightType());
        if (route.deactivationDate() != null) {
            System.out.printf("Deactivation date    : %s%n", route.deactivationDate().value());
        }

        if (route.flightType() == FlightType.CHARTER) {
            final RouteSchedule schedule = route.routeSchedule();
            if (schedule != null) {
                System.out.printf("Scheduled departure  : %s%n", schedule.scheduledDeparture());
                System.out.printf("Scheduled arrival    : %s%n", schedule.scheduledArrival());
            }
        } else {
            final RouteRecurringSchedule recurring = route.routeRecurringSchedule();
            if (recurring != null) {
                final String days = recurring.entries().stream()
                        .map(RecurringScheduleEntry::dayOfWeek)
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                System.out.printf("Recurring days       : %s%n", days);
            }
        }
        System.out.println(SEPARATOR);
    }

    public static void printDeactivationSummary(final Route route) {
        System.out.println();
        System.out.println("Route deactivated successfully.");
        System.out.println(SEPARATOR);
        System.out.printf("Route name           : %s%n", route.identity());
        System.out.printf("Company IATA code    : %s%n", route.companyIATACode());
        if (route.deactivationDate() != null) {
            System.out.printf("Deactivation date    : %s%n", route.deactivationDate().value());
        }
        System.out.println(SEPARATOR);
    }
}
