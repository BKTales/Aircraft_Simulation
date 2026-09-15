package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanController;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanRequest;
import eapli.aisafe.flightmanagement.application.CreateFlightPlanResult;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.OperationalSuffix;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("squid:S106")
public class CreateFlightPlanUI extends AbstractUI {

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

    private final CreateFlightPlanController controller;

    public CreateFlightPlanUI() {
        this(new CreateFlightPlanController());
    }

    CreateFlightPlanUI(final CreateFlightPlanController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

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

            final List<Route> routes = controller.listSelectableRoutes(departure.toLocalDate());
            if (routes.isEmpty()) {
                System.out.println("No active routes available for your company on the selected date.");
                return false;
            }

            final Route route = chooseRoute(routes);
            if (route == null) {
                return false;
            }

            final List<String> aircraftRegs = controller.listCompanyActiveAircraftRegistrations();
            if (aircraftRegs.isEmpty()) {
                System.out.println("No active aircraft in your company fleet.");
                return false;
            }
            final String aircraftReg = chooseAircraft(aircraftRegs);
            if (aircraftReg == null) {
                return false;
            }

            final List<PilotUser> pilots = controller.listCompanyPilots();
            if (pilots.isEmpty()) {
                System.out.println("No active pilots in your company roster.");
                return false;
            }
            final PilotUser pilot = choosePilot(pilots);
            if (pilot == null) {
                return false;
            }

            final FuelQuantity fuel = readFuel();
            if (fuel == null) {
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

            final CreateFlightPlanRequest request = new CreateFlightPlanRequest(
                    route.identity().toString(),
                    aircraftReg,
                    pilot.systemUser().username().toString(),
                    departure,
                    arrival,
                    fuel,
                    passengers,
                    paxWeight,
                    cargoWeight,
                    suffix);

            CreateFlightPlanResult result = controller.createFlightPlan(request);
            if (result.needsConfirmation()) {
                System.out.println(result.errorMessage());
                final String answer = Console.readLine("");
                if (answer != null && answer.trim().equalsIgnoreCase("y")) {
                    result = controller.createFlightPlan(new CreateFlightPlanRequest(
                            route.identity().toString(),
                            aircraftReg,
                            pilot.systemUser().username().toString(),
                            departure,
                            arrival,
                            fuel,
                            passengers,
                            paxWeight,
                            cargoWeight,
                            suffix,
                            true));
                } else {
                    System.out.println("Replacement cancelled.");
                    return false;
                }
            } else if (!result.isSuccess() && suffix.isEmpty()) {
                System.out.println(result.errorMessage());
                suffixInput = Console.readLine("Try an operational suffix (one letter) or leave empty to cancel:");
                if (suffixInput != null && !suffixInput.isBlank()) {
                    suffix = OperationalSuffix.optionalOf(suffixInput);
                    result = controller.createFlightPlan(new CreateFlightPlanRequest(
                            route.identity().toString(),
                            aircraftReg,
                            pilot.systemUser().username().toString(),
                            departure,
                            arrival,
                            fuel,
                            passengers,
                            paxWeight,
                            cargoWeight,
                            suffix));
                }
            }

            if (result.isSuccess()) {
                FlightPlanCreationSummary.print(
                        result,
                        route,
                        aircraftReg,
                        pilot.systemUser().name().firstName() + " " + pilot.systemUser().name().lastName(),
                        departure,
                        arrival,
                        fuel,
                        passengers,
                        paxWeight,
                        cargoWeight);
                return true;
            }

            System.out.println("\nCould not create flight plan: " + result.errorMessage());
            return false;
        } catch (final IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private Route chooseRoute(final List<Route> routes) {
        System.out.println("\nAvailable routes:");
        for (int i = 0; i < routes.size(); i++) {
            final Route r = routes.get(i);
            System.out.println("  " + (i + 1) + ") " + r.identity() + "  "
                    + r.originAirport().identity() + " -> " + r.destinationAirport().identity()
                    + "  (" + r.flightType() + ")");
        }
        final int choice = Console.readInteger("\nChoose route number:");
        if (choice < 1 || choice > routes.size()) {
            return null;
        }
        return routes.get(choice - 1);
    }

    private String chooseAircraft(final List<String> regs) {
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

    private PilotUser choosePilot(final List<PilotUser> pilots) {
        System.out.println("\nCompany pilots:");
        for (int i = 0; i < pilots.size(); i++) {
            final PilotUser p = pilots.get(i);
            System.out.println("  " + (i + 1) + ") " + p.systemUser().name().firstName() + " "
                    + p.systemUser().name().lastName() + " (" + p.systemUser().email() + ")");
        }
        final int choice = Console.readInteger("\nChoose pilot number:");
        if (choice < 1 || choice > pilots.size()) {
            return null;
        }
        return pilots.get(choice - 1);
    }

    private FuelQuantity readFuel() {
        final double amount = Console.readDouble("Fuel quantity:");
        final String unit = Console.readLine("Fuel unit (kg or l):");
        if (unit == null || unit.isBlank()) {
            System.out.println("Fuel unit is required.");
            return null;
        }
        try {
            return new FuelQuantity(amount, unit);
        } catch (final IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
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
