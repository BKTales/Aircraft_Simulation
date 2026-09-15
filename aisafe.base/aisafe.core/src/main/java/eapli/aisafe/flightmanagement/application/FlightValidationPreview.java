package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;

import java.time.LocalDateTime;

/**
 * Read-only summary of a flight a pilot may submit for validation (US085).
 * Carries the stored DSL so the UI can render parse errors without a second query.
 */
public record FlightValidationPreview(
        String designator,
        String routeName,
        String originIata,
        String destinationIata,
        String aircraftRegistration,
        FlightPlanStatus status,
        LocalDateTime scheduledDeparture,
        String dslContent) {

    public String routeLabel() {
        if (originIata == null || destinationIata == null) {
            return routeName == null ? "" : routeName;
        }
        return routeName + " (" + originIata + " -> " + destinationIata + ")";
    }
}
