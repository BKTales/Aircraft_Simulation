package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
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

/**
 * US073 — same flow as backoffice {@link eapli.aisafe.app.backoffice.console.presentation.flightroute.CreateRouteUI}, over TCP.
 */
@SuppressWarnings("squid:S106")
public final class CreateRouteRemoteUI extends AbstractUI {

    private static final DateTimeFormatter FLEXIBLE_LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    protected boolean doShow() {
        try {
            final String companyIata = requireCompanyContext();
            final String routeName = chooseValidRouteName(companyIata);

            final List<RemoteAirportEntry> airports = loadAirports();
            if (airports.size() < 2) {
                System.out.println("At least two airports are required to create a route.");
                return false;
            }

            final RemoteAirportEntry origin = chooseAirport("Select origin airport:", airports);
            if (origin == null) {
                return false;
            }

            final List<RemoteAirportEntry> destinationCandidates = airports.stream()
                    .filter(a -> !a.iata().equals(origin.iata()))
                    .sorted(Comparator.comparing(RemoteAirportEntry::iata))
                    .toList();
            final RemoteAirportEntry destination = chooseAirport("Select destination airport:", destinationCandidates);
            if (destination == null) {
                return false;
            }

            final String flightType = chooseFlightType();
            if (flightType == null) {
                System.out.println("Invalid flight type.");
                return false;
            }

            final String payload;
            if ("CHARTER".equals(flightType)) {
                final LocalDate departure = readLocalDate("Scheduled departure date");
                final LocalDate arrival = readLocalDate("Scheduled arrival date");
                payload = String.join(";",
                        routeName, origin.iata(), destination.iata(),
                        flightType, departure.toString(), arrival.toString());
            } else {
                payload = String.join(";",
                        routeName, origin.iata(), destination.iata(),
                        flightType, chooseRecurringDaysToken());
            }

            RemoteRouteCreationSummary.printResponse(
                    RemoteTcpGateway.request(AtccOpcodes.CREATE_ROUTE, payload));
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private String requireCompanyContext() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.ROUTE_COMPANY_CONTEXT, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            throw new IllegalStateException("Could not resolve company context.");
        }
        final String company = resp.payload().trim();
        if (company.isEmpty()) {
            throw new IllegalStateException("Could not resolve company context.");
        }
        return company;
    }

    private String chooseValidRouteName(final String companyIata) throws IOException {
        while (true) {
            final String suffix = Console.readLine(
                    "Route numeric suffix (1 to 4 digits) for company " + companyIata);
            if (!suffix.trim().matches("\\d{1,4}")) {
                System.out.println("Route numeric suffix must be 1 to 4 digits.");
                continue;
            }
            final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.VALIDATE_ROUTE_NAME, suffix.trim());
            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                return resp.payload().trim();
            }
            System.out.println(RemoteTcpGateway.messageFrom(resp, "Route name validation failed."));
        }
    }

    private List<RemoteAirportEntry> loadAirports() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.LIST_ROUTE_AIRPORTS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return List.of();
        }
        return RemoteAirportEntry.parsePayload(resp.payload());
    }

    private RemoteAirportEntry chooseAirport(final String prompt, final List<RemoteAirportEntry> airports) {
        if (airports.isEmpty()) {
            return null;
        }
        System.out.println(prompt);
        RemoteAirportPrinter.printHeader();
        final SelectWidget<RemoteAirportEntry> selector =
                new SelectWidget<>("", airports, new RemoteAirportPrinter());
        selector.show();
        return selector.selectedElement();
    }

    private String chooseFlightType() {
        System.out.println("Flight type:");
        System.out.println("1 - REGULAR");
        System.out.println("2 - CHARTER");
        final int option = Console.readInteger("Option");
        return switch (option) {
            case 1 -> "REGULAR";
            case 2 -> "CHARTER";
            default -> null;
        };
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

    private String chooseRecurringDaysToken() {
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
        return days.stream().map(DayOfWeek::name).reduce((a, b) -> a + "," + b).orElse("");
    }

    @Override
    public String headline() {
        return "Create Route (US073)";
    }
}
