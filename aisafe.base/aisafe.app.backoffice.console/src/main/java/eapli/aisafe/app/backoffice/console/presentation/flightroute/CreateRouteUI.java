package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.routemanagement.application.CreateRouteController;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import eapli.framework.visitor.Visitor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("squid:S106")
public class CreateRouteUI extends AbstractUI {

    private static final DateTimeFormatter FLEXIBLE_LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private final CreateRouteController controller;

    public CreateRouteUI() {
        this(new CreateRouteController());
    }

    public CreateRouteUI(final CreateRouteController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    protected boolean doShow() {
        try {
            final String companyIATACode = controller.currentCompanyContext();
            final String routeName = chooseValidRouteName(companyIATACode);

            final List<Airport> airports = loadAirports();
            if (airports.size() < 2) {
                System.out.println("At least two airports are required to create a route.");
                return false;
            }

            final Airport origin = chooseAirport("Select origin airport:", airports);
            if (origin == null) {
                return false;
            }

            final List<Airport> destinationCandidates = airports.stream()
                    .filter(a -> !a.identity().equals(origin.identity()))
                    .sorted(Comparator.comparing(a -> a.identity().toString()))
                    .toList();
            final Airport destination = chooseAirport("Select destination airport:", destinationCandidates);
            if (destination == null) {
                return false;
            }

            final FlightType flightType = chooseFlightType();
            if (flightType == null) {
                System.out.println("Invalid flight type.");
                return false;
            }

            final Route created;
            if (flightType == FlightType.CHARTER) {
                final LocalDate departure = readLocalDate("Scheduled departure date");
                final LocalDate arrival = readLocalDate("Scheduled arrival date");
                created = controller.createCharterRoute(
                        routeName,
                        origin.identity().toString(),
                        destination.identity().toString(),
                        departure,
                        arrival);
            } else {
                final List<DayOfWeek> recurringDays = chooseRecurringDays();
                created = controller.createRegularRoute(
                        routeName,
                        origin.identity().toString(),
                        destination.identity().toString(),
                        recurringDays);
            }

            RoutePrinter.printCreationSummary(created);
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private LocalDate readLocalDate(final String prompt) {
        while (true) {
            final String text = Console.readLine(prompt + " (YYYY-MM-DD, e.g. 2026-07-15 or 2026-7-15)");
            try {
                return LocalDate.parse(text.trim(), FLEXIBLE_LOCAL_DATE);
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid date. Use year-month-day separated by '-' (e.g. 2026-07-15).");
            }
        }
    }

    private String chooseValidRouteName(final String companyIATACode) {
        while (true) {
            final String numericSuffix = Console.readLine(
                    "Route numeric suffix (1 to 4 digits) for company " + companyIATACode);
            try {
                return controller.validateRouteName(numericSuffix, companyIATACode);
            } catch (final IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private List<Airport> loadAirports() {
        final List<Airport> list = new ArrayList<>();
        controller.listAirports().forEach(list::add);
        list.sort(Comparator.comparing(a -> a.identity().toString()));
        return list;
    }

    private Airport chooseAirport(final String prompt, final List<Airport> airports) {
        if (airports.isEmpty()) {
            return null;
        }
        System.out.println(prompt);
        final SelectWidget<Airport> selector = new SelectWidget<>("", airports, new AirportPrinter());
        selector.show();
        return selector.selectedElement();
    }

    private FlightType chooseFlightType() {
        System.out.println("Flight type:");
        System.out.println("1 - REGULAR");
        System.out.println("2 - CHARTER");
        final int option = Console.readInteger("Option");
        return switch (option) {
            case 1 -> FlightType.REGULAR;
            case 2 -> FlightType.CHARTER;
            default -> null;
        };
    }

    private List<DayOfWeek> chooseRecurringDays() {
        final List<DayOfWeek> days = new ArrayList<>();
        while (true) {
            System.out.println("Recurring day (1=MON ... 7=SUN, 0=finish):");
            final int option = Console.readInteger("Day");
            if (option == 0) {
                break;
            }
            final DayOfWeek day = switch (option) {
                case 1 -> DayOfWeek.MONDAY;
                case 2 -> DayOfWeek.TUESDAY;
                case 3 -> DayOfWeek.WEDNESDAY;
                case 4 -> DayOfWeek.THURSDAY;
                case 5 -> DayOfWeek.FRIDAY;
                case 6 -> DayOfWeek.SATURDAY;
                case 7 -> DayOfWeek.SUNDAY;
                default -> null;
            };
            if (day == null) {
                System.out.println("Invalid day.");
                continue;
            }
            if (days.contains(day)) {
                System.out.println("Day already selected.");
                continue;
            }
            days.add(day);
        }
        if (days.isEmpty()) {
            throw new IllegalArgumentException("At least one recurring day is required for regular routes.");
        }
        return days;
    }

    @Override
    public String headline() {
        return "Create Route (US073)";
    }

    private static final class AirportPrinter implements Visitor<Airport> {
        @Override
        public void visit(final Airport visitee) {
            System.out.printf("%-6s %-6s area=%s%n",
                    visitee.identity(),
                    visitee.icaoCode(),
                    visitee.airControlAreaCode());
        }
    }
}
