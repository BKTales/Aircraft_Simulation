package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.aircraftmanagement.application.FleetListCriteria;
import eapli.aisafe.aircraftmanagement.application.FleetNumericComparison;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;

public final class FleetCriteriaParser {

    private FleetCriteriaParser() {}

    public static FleetListCriteria parse(final String payload) {
        if (payload == null || payload.isBlank() || "UNFILTERED".equalsIgnoreCase(payload.trim())) {
            return FleetListCriteria.unfiltered();
        }
        final String[] parts = payload.trim().split(";", -1);
        final String type = parts[0].trim().toUpperCase();
        return switch (type) {
            case "MODEL" -> {
                require(parts, 2, "MODEL;modelId");
                yield FleetListCriteria.byModel(AircraftModelId.valueOf(parts[1].trim()));
            }
            case "MANUFACTURER" -> {
                require(parts, 2, "MANUFACTURER;manufacturerId");
                yield FleetListCriteria.byManufacturer(ManufacturerId.valueOf(parts[1].trim()));
            }
            case "PASSENGERS" -> {
                require(parts, 3, "PASSENGERS;value;EQ|GT|LT");
                yield FleetListCriteria.byPassengerCapacity(
                        Integer.parseInt(parts[1].trim()), parseComparison(parts[2].trim()));
            }
            case "AGE" -> {
                require(parts, 3, "AGE;years;EQ|GT|LT");
                yield FleetListCriteria.byAge(Integer.parseInt(parts[1].trim()), parseComparison(parts[2].trim()));
            }
            default -> throw new IllegalArgumentException("Unknown fleet filter: " + type);
        };
    }

    private static void require(final String[] parts, final int min, final String example) {
        if (parts.length < min) {
            throw new IllegalArgumentException("Expected format: " + example);
        }
    }

    private static FleetNumericComparison parseComparison(final String token) {
        return switch (token.toUpperCase()) {
            case "EQ", "EQUAL" -> FleetNumericComparison.EQUAL;
            case "GT", "GREATER_THAN" -> FleetNumericComparison.GREATER_THAN;
            case "LT", "LESS_THAN" -> FleetNumericComparison.LESS_THAN;
            default -> throw new IllegalArgumentException("Comparison must be EQ, GT, or LT");
        };
    }
}
