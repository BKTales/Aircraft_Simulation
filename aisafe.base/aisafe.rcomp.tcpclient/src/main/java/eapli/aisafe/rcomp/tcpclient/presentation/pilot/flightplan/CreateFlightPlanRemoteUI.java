package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.aisafe.flightmanagement.domain.OperationalSuffix;
import eapli.aisafe.rcomp.protocol.PilotCreateFlightPlanPayload;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

/**
 * US080 / US086 — same flow as backoffice {@code CreateFlightPlanUI}, over TCP.
 */
@SuppressWarnings("squid:S106")
public final class CreateFlightPlanRemoteUI extends AbstractUI {

    /** Accepts user input with or without zero-padded month/day. */
    private static final DateTimeFormatter FLEXIBLE_DATE_TIME = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    /** Wire format for CREATE_FLIGHT_PLAN — must match {@link PilotCreateFlightPlanPayload}. */
    private static final DateTimeFormatter WIRE_DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    @Override
    protected boolean doShow() {
        try {
            final LocalDateTime departure = readDateTime("Scheduled departure (yyyy-MM-dd HH:mm):");
            if (departure == null) {
                return false;
            }
            final LocalDateTime arrival = readDateTime("Scheduled arrival (yyyy-MM-dd HH:mm):");
            if (arrival == null) {
                return false;
            }
            if (!arrival.isAfter(departure)) {
                System.out.println("Arrival must be after departure.");
                return false;
            }

            final RemoteRouteEntry route = chooseRoute(departure);
            if (route == null) {
                return false;
            }

            final String aircraftReg = chooseAircraft();
            if (aircraftReg == null) {
                return false;
            }

            final RemotePilotEntry pilot = choosePilot();
            if (pilot == null) {
                return false;
            }

            final double fuelAmount = Console.readDouble("Fuel quantity:");
            final String fuelUnit = Console.readLine("Fuel unit (kg or l):");
            if (fuelUnit == null || fuelUnit.isBlank()) {
                System.out.println("Fuel unit is required.");
                return false;
            }

            final int passengers = Console.readInteger("Passenger count:");
            final double paxWeight = Console.readDouble("Total passenger weight (kg):");
            final double cargoWeight = Console.readDouble("Cargo weight (kg):");

            Optional<OperationalSuffix> suffix = Optional.empty();
            String suffixInput = Console.readLine("Operational suffix (one letter, or empty):");
            if (suffixInput != null && !suffixInput.isBlank()) {
                suffix = OperationalSuffix.optionalOf(suffixInput);
            }

            ProtocolFrame resp = submit(route.routeName(), aircraftReg, pilot.username(),
                    departure, arrival, fuelAmount, fuelUnit, passengers, paxWeight, cargoWeight, suffix, false);

            if (resp != null && resp.opcode() == ResponseCodes.NEEDS_CONFIRMATION) {
                System.out.println(resp.payload());
                final String answer = Console.readLine("");
                if (answer != null && answer.trim().equalsIgnoreCase("y")) {
                    resp = submit(route.routeName(), aircraftReg, pilot.username(),
                            departure, arrival, fuelAmount, fuelUnit, passengers, paxWeight, cargoWeight, suffix, true);
                } else {
                    System.out.println("Replacement cancelled.");
                    return false;
                }
            } else if (resp != null && resp.opcode() == ResponseCodes.BAD_REQUEST && suffix.isEmpty()) {
                System.out.println(resp.payload());
                suffixInput = Console.readLine("Try an operational suffix (one letter) or leave empty to cancel:");
                if (suffixInput != null && !suffixInput.isBlank()) {
                    suffix = OperationalSuffix.optionalOf(suffixInput);
                    resp = submit(route.routeName(), aircraftReg, pilot.username(),
                            departure, arrival, fuelAmount, fuelUnit, passengers, paxWeight, cargoWeight, suffix, false);
                }
            }

            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                RemoteFlightPlanCreationSummary.printSuccess(
                        route,
                        aircraftReg,
                        pilot.firstName() + " " + pilot.lastName(),
                        departure,
                        arrival,
                        fuelAmount,
                        fuelUnit,
                        passengers,
                        paxWeight,
                        cargoWeight,
                        resp);
                return true;
            }

            System.out.println("\nCould not create flight plan: "
                    + RemoteTcpGateway.messageFrom(resp, "Request failed."));
            return false;
        } catch (final IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return false;
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
            return false;
        }
    }

    private ProtocolFrame submit(final String routeName,
                                 final String aircraftReg,
                                 final String pilotUsername,
                                 final LocalDateTime departure,
                                 final LocalDateTime arrival,
                                 final double fuelAmount,
                                 final String fuelUnit,
                                 final int passengers,
                                 final double paxWeight,
                                 final double cargoWeight,
                                 final Optional<OperationalSuffix> suffix,
                                 final boolean confirmReplace) throws IOException {
        final String payload = PilotCreateFlightPlanPayload.encode(new PilotCreateFlightPlanPayload.Fields(
                routeName,
                aircraftReg,
                pilotUsername,
                departure.format(WIRE_DATE_TIME),
                arrival.format(WIRE_DATE_TIME),
                fuelAmount,
                fuelUnit.trim(),
                passengers,
                paxWeight,
                cargoWeight,
                suffix.map(OperationalSuffix::letter).orElse(""),
                confirmReplace));
        return RemoteTcpGateway.request(PilotOpcodes.CREATE_FLIGHT_PLAN, payload);
    }

    private RemoteRouteEntry chooseRoute(final LocalDateTime departure) throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(
                PilotOpcodes.LIST_CREATE_ROUTES, departure.toLocalDate().toString());
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<RemoteRouteEntry> routes = RemoteRouteEntry.parseLines(resp.payload());
        if (routes.isEmpty()) {
            System.out.println("No active routes available for your company on the selected date.");
            return null;
        }
        System.out.println("\nAvailable routes:");
        for (int i = 0; i < routes.size(); i++) {
            final RemoteRouteEntry route = routes.get(i);
            System.out.println("  " + (i + 1) + ") " + route.routeName() + "  "
                    + route.origin() + " -> " + route.destination()
                    + "  (" + route.flightType() + ")");
        }
        final int choice = Console.readInteger("\nChoose route number:");
        if (choice < 1 || choice > routes.size()) {
            return null;
        }
        return routes.get(choice - 1);
    }

    private String chooseAircraft() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(PilotOpcodes.LIST_IMPORT_AIRCRAFT, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> regs = resp.payload().lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
        if (regs.isEmpty()) {
            System.out.println("No active aircraft in your company fleet.");
            return null;
        }
        System.out.println("\nActive aircraft:");
        for (int i = 0; i < regs.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + regs.get(i));
        }
        final int choice = Console.readInteger("\nChoose aircraft number:");
        if (choice < 1 || choice > regs.size()) {
            return null;
        }
        return regs.get(choice - 1);
    }

    private RemotePilotEntry choosePilot() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(PilotOpcodes.LIST_COMPANY_PILOTS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<RemotePilotEntry> pilots = RemotePilotEntry.parseLines(resp.payload());
        if (pilots.isEmpty()) {
            System.out.println("No active pilots in your company roster.");
            return null;
        }
        System.out.println("\nCompany pilots:");
        for (int i = 0; i < pilots.size(); i++) {
            final RemotePilotEntry pilot = pilots.get(i);
            System.out.println("  " + (i + 1) + ") " + pilot.firstName() + " "
                    + pilot.lastName() + " (" + pilot.email() + ")");
        }
        final int choice = Console.readInteger("\nChoose pilot number:");
        if (choice < 1 || choice > pilots.size()) {
            return null;
        }
        return pilots.get(choice - 1);
    }

    private LocalDateTime readDateTime(final String prompt) {
        final String raw = Console.readLine(prompt);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw.trim(), FLEXIBLE_DATE_TIME);
        } catch (final DateTimeParseException ex) {
            System.out.println("Invalid date/time. Use yyyy-MM-dd HH:mm");
            return null;
        }
    }

    @Override
    public String headline() {
        return "Create Flight Plan (US080)";
    }
}
