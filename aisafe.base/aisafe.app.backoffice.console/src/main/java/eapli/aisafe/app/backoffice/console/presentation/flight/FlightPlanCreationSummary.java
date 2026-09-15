package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.aisafe.flightmanagement.application.CreateFlightPlanResult;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.routemanagement.domain.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Optional;

@SuppressWarnings("squid:S106")
public final class FlightPlanCreationSummary {

    private static final String SEPARATOR = "----------------------------------------";
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private FlightPlanCreationSummary() {}

    public static void print(final CreateFlightPlanResult result,
                             final Route route,
                             final String aircraftReg,
                             final String pilotDisplayName,
                             final LocalDateTime departure,
                             final LocalDateTime arrival,
                             final FuelQuantity fuel,
                             final int passengerCount,
                             final double passengerWeightKg,
                             final double cargoWeightKg) {
        if (!result.isSuccess()) {
            return;
        }

        final String designator = result.designator().orElseThrow().toString();
        final String headline = result.replaced()
                ? "Flight plan replaced successfully."
                : "Flight plan created successfully.";

        System.out.println();
        System.out.println(headline);
        System.out.println(SEPARATOR);
        System.out.printf("Designator           : %s%n", designator);
        System.out.printf("Plan status          : DRAFT%n");
        System.out.printf("Route                : %s  %s -> %s  (%s)%n",
                route.identity(),
                route.originAirport().identity(),
                route.destinationAirport().identity(),
                route.flightType());
        System.out.printf("Aircraft             : %s%n", aircraftReg);
        System.out.printf("Pilot                : %s%n", pilotDisplayName);
        System.out.printf("Scheduled departure  : %s%n", departure.format(DATE_TIME));
        System.out.printf("Scheduled arrival    : %s%n", arrival.format(DATE_TIME));
        System.out.printf("Fuel                 : %.2f %s%n", fuel.amount(), fuel.unit());
        System.out.printf("Passengers           : %d%n", passengerCount);
        System.out.printf("Passenger weight     : %.2f kg%n", passengerWeightKg);
        System.out.printf("Cargo weight         : %.2f kg%n", cargoWeightKg);
        System.out.printf("Total payload        : %.2f kg%n", passengerWeightKg + cargoWeightKg);

        replacementNote(result).ifPresent(note -> System.out.printf("Note                 : %s%n", note));

        System.out.println(SEPARATOR);
    }

    private static Optional<String> replacementNote(final CreateFlightPlanResult result) {
        if (!result.replaced()) {
            return Optional.empty();
        }
        return result.replacedFromStatus().map(FlightPlanCreationSummary::noteForStatus);
    }

    private static String noteForStatus(final FlightPlanStatus previousStatus) {
        return switch (previousStatus) {
            case DRAFT -> "Previous DRAFT flight plan was replaced automatically.";
            case SIM_REJECTED -> "Previous SIM_REJECTED flight plan was replaced automatically.";
            case SIM_APPROVED -> "Previous SIM_APPROVED flight plan was replaced at your request.";
            case SUBMITTED_FOR_SIMULATION ->
                    "Previous flight plan (submitted for simulation) was replaced at your request.";
        };
    }
}
