package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Optional;

@SuppressWarnings("squid:S106")
public final class RemoteFlightPlanCreationSummary {

    private static final String SEPARATOR = "----------------------------------------";
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private RemoteFlightPlanCreationSummary() {}

    public static void printSuccess(final RemoteRouteEntry route,
                                    final String aircraftReg,
                                    final String pilotDisplayName,
                                    final LocalDateTime departure,
                                    final LocalDateTime arrival,
                                    final double fuelAmount,
                                    final String fuelUnit,
                                    final int passengerCount,
                                    final double passengerWeightKg,
                                    final double cargoWeightKg,
                                    final ProtocolFrame resp) {
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return;
        }
        final String payload = resp.payload().trim();
        final String body = payload.startsWith("OK|") ? payload.substring(3) : payload;
        final String[] fields = body.split("\\|", -1);
        final String designator = fields.length > 0 ? fields[0] : "";
        final String action = fields.length > 2 ? fields[2] : "CREATED";
        final boolean replaced = "REPLACED".equals(action);
        final Optional<FlightPlanStatus> replacedFrom = replaced && fields.length > 3 && !fields[3].isBlank()
                ? parseStatus(fields[3])
                : Optional.empty();

        final String headline = replaced
                ? "Flight plan replaced successfully."
                : "Flight plan created successfully.";

        System.out.println();
        System.out.println(headline);
        System.out.println(SEPARATOR);
        System.out.printf("Designator           : %s%n", designator);
        System.out.printf("Plan status          : DRAFT%n");
        System.out.printf("Route                : %s  %s -> %s  (%s)%n",
                route.routeName(), route.origin(), route.destination(), route.flightType());
        System.out.printf("Aircraft             : %s%n", aircraftReg);
        System.out.printf("Pilot                : %s%n", pilotDisplayName);
        System.out.printf("Scheduled departure  : %s%n", departure.format(DATE_TIME));
        System.out.printf("Scheduled arrival    : %s%n", arrival.format(DATE_TIME));
        System.out.printf("Fuel                 : %.2f %s%n", fuelAmount, fuelUnit.trim().toLowerCase());
        System.out.printf("Passengers           : %d%n", passengerCount);
        System.out.printf("Passenger weight     : %.2f kg%n", passengerWeightKg);
        System.out.printf("Cargo weight         : %.2f kg%n", cargoWeightKg);
        System.out.printf("Total payload        : %.2f kg%n", passengerWeightKg + cargoWeightKg);

        replacementNote(replacedFrom).ifPresent(note -> System.out.printf("Note                 : %s%n", note));

        System.out.println(SEPARATOR);
    }

    private static Optional<FlightPlanStatus> parseStatus(final String raw) {
        try {
            return Optional.of(FlightPlanStatus.valueOf(raw.trim()));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> replacementNote(final Optional<FlightPlanStatus> previousStatus) {
        return previousStatus.map(status -> switch (status) {
            case DRAFT -> "Previous DRAFT flight plan was replaced automatically.";
            case SIM_REJECTED -> "Previous SIM_REJECTED flight plan was replaced automatically.";
            case SIM_APPROVED -> "Previous SIM_APPROVED flight plan was replaced at your request.";
            case SUBMITTED_FOR_SIMULATION ->
                    "Previous flight plan (submitted for simulation) was replaced at your request.";
        });
    }
}
