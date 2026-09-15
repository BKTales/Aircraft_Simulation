package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.routemanagement.domain.RecurringScheduleEntry;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;

import java.util.stream.Collectors;

public final class AtccResponseFormatter {

    private AtccResponseFormatter() {}

    public static String formatAircraft(final Aircraft aircraft) {
        return String.join("|",
                aircraft.identity().toString(),
                aircraft.aircraftModelId().toString(),
                aircraft.operationalStatus().name(),
                String.valueOf(aircraft.cabinConfiguration().totalSeats()),
                String.valueOf(aircraft.ageInYears()));
    }

    public static String formatAircraftModel(final AircraftModel model) {
        return String.join("|",
                model.identity().toString(),
                model.name().toString().trim(),
                model.manufacturer().toString(),
                model.aircraftType().name());
    }

    public static String formatModelId(final AircraftModelId id) {
        return id.toString();
    }

    public static String formatManufacturerId(final ManufacturerId id) {
        return id.toString();
    }

    public static String formatPilot(final ResponsePilotCollaboratorDTO pilot) {
        return String.join("|",
                pilot.getEmail(),
                pilot.getFirstName(),
                pilot.getLastName(),
                "ACTIVE",
                String.valueOf(pilot.getCertificationCount()));
    }

    public static String formatAirport(final Airport airport) {
        return String.join("|",
                airport.identity().toString(),
                airport.icaoCode().toString(),
                airport.airControlAreaCode());
    }

    public static String formatRoute(final Route route) {
        final String schedule = route.routeSchedule() == null
                ? "-"
                : route.routeSchedule().scheduledDeparture() + "->" + route.routeSchedule().scheduledArrival();
        final String recurring = route.routeRecurringSchedule() == null
                ? "-"
                : route.routeRecurringSchedule().entries().stream()
                        .map(RecurringScheduleEntry::dayOfWeek)
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
        final String deactivation = route.deactivationDate() == null
                ? "-"
                : route.deactivationDate().value().toString();
        return String.join("|",
                route.identity().toString(),
                route.companyIATACode().toString(),
                route.originAirportIATACode().toString(),
                route.destinationAirportIATACode().toString(),
                route.flightType().name(),
                schedule,
                recurring,
                deactivation);
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
