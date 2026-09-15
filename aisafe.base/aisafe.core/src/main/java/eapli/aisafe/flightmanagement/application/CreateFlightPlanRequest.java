package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.FuelQuantity;
import eapli.aisafe.flightmanagement.domain.OperationalSuffix;

import java.time.LocalDateTime;
import java.util.Optional;

public record CreateFlightPlanRequest(
        String routeName,
        String aircraftRegistration,
        String pilotUsername,
        LocalDateTime departure,
        LocalDateTime arrival,
        FuelQuantity fuel,
        int passengerCount,
        double passengerWeightKg,
        double cargoWeightKg,
        Optional<OperationalSuffix> operationalSuffix,
        boolean confirmReplace) {

    public CreateFlightPlanRequest {
        if (routeName == null || routeName.isBlank()) {
            throw new IllegalArgumentException("Route name is required.");
        }
        if (aircraftRegistration == null || aircraftRegistration.isBlank()) {
            throw new IllegalArgumentException("Aircraft registration is required.");
        }
        if (pilotUsername == null || pilotUsername.isBlank()) {
            throw new IllegalArgumentException("Pilot username is required.");
        }
        if (departure == null || arrival == null) {
            throw new IllegalArgumentException("Departure and arrival are required.");
        }
        if (fuel == null) {
            throw new IllegalArgumentException("Fuel quantity is required.");
        }
        if (passengerCount < 0 || passengerWeightKg < 0 || cargoWeightKg < 0) {
            throw new IllegalArgumentException("Load values cannot be negative.");
        }
        if (operationalSuffix == null) {
            operationalSuffix = Optional.empty();
        }
    }

    /** Convenience constructor without explicit replace confirmation (defaults to false). */
    public CreateFlightPlanRequest(final String routeName,
                                   final String aircraftRegistration,
                                   final String pilotUsername,
                                   final LocalDateTime departure,
                                   final LocalDateTime arrival,
                                   final FuelQuantity fuel,
                                   final int passengerCount,
                                   final double passengerWeightKg,
                                   final double cargoWeightKg,
                                   final Optional<OperationalSuffix> operationalSuffix) {
        this(routeName, aircraftRegistration, pilotUsername, departure, arrival, fuel,
                passengerCount, passengerWeightKg, cargoWeightKg, operationalSuffix, false);
    }
}
