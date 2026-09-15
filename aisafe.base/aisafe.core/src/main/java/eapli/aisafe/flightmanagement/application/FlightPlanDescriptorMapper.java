package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightLoad;
import eapli.aisafe.flightmanagement.domain.FlightPlan;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.domain.FuelLoad;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

public final class FlightPlanDescriptorMapper {

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final double LITRES_TO_KG = 0.804;

    private FlightPlanDescriptorMapper() {}

    public static Flight toFlight(final FlightPlanDescriptor descriptor,
                                  final String dslContent,
                                  final PilotUser pilot,
                                  final Aircraft aircraft,
                                  final SimulatorFlightPlanJsonBuilder jsonBuilder) {
        if (descriptor.getLegs().isEmpty()) {
            throw new IllegalArgumentException("Flight plan must have at least one leg.");
        }

        aircraft.assertAssignableToNewFlight();

        final String routeName = routeName(descriptor);
        final Flight flight = new Flight(
                new FlightDesignator(descriptor.getFlightId()),
                routeName,
                aircraft.identity().toString());

        flight.assignPilotId(pilot);
        flight.assignSchedule(buildSchedule(descriptor.getLegs()));
        flight.assignFlightLoad(buildLoad(descriptor));

        final LegDescriptor firstLeg = descriptor.getLegs().get(0);
        final String jsonContent = jsonBuilder.toSimulatorJson(descriptor, aircraft);

        flight.assignFlightPlan(FlightPlan.forFlight(
                flight.designator(),
                FlightPlanStatus.DRAFT,
                fuelLoadKg(firstLeg),
                dslContent,
                jsonContent));

        return flight;
    }

    private static FlightType toFlightType(final String dslType) {
        if ("CHARTER".equalsIgnoreCase(dslType)) {
            return FlightType.CHARTER;
        }
        return FlightType.REGULAR;
    }

    private static String routeName(final FlightPlanDescriptor descriptor) {
        final List<LegDescriptor> legs = descriptor.getLegs();
        return legs.get(0).getDepartureAirport() + "-" + legs.get(legs.size() - 1).getArrivalAirport();
    }

    private static FlightSchedule buildSchedule(final List<LegDescriptor> legs) {
        LocalDateTime minDep = parseDateTime(legs.get(0).getDepartureTime());
        LocalDateTime maxArr = parseDateTime(legs.get(0).getArrivalTime());
        for (LegDescriptor leg : legs) {
            final LocalDateTime dep = parseDateTime(leg.getDepartureTime());
            final LocalDateTime arr = parseDateTime(leg.getArrivalTime());
            if (dep.isBefore(minDep)) {
                minDep = dep;
            }
            if (arr.isAfter(maxArr)) {
                maxArr = arr;
            }
        }
        return new FlightSchedule(minDep, maxArr);
    }

    private static FlightLoad buildLoad(final FlightPlanDescriptor descriptor) {
        return new FlightLoad(
                descriptor.getPassengersCount(),
                descriptor.getPassengerWeightKg(),
                descriptor.getCargoWeightKg());
    }

    private static FuelLoad fuelLoadKg(final LegDescriptor leg) {
        double qty = leg.getFuelValue();
        if ("l".equalsIgnoreCase(leg.getFuelUnit())) {
            qty = qty * LITRES_TO_KG;
        }
        return new FuelLoad(qty);
    }

    private static LocalDateTime parseDateTime(final String value) {
        return LocalDateTime.parse(value, DT);
    }
}
