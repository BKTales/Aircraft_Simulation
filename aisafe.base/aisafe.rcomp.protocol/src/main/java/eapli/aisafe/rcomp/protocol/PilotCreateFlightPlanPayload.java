package eapli.aisafe.rcomp.protocol;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Encodes/decodes CREATE_FLIGHT_PLAN (US080) semicolon payloads for Pilot remote access. */
public final class PilotCreateFlightPlanPayload {

    private static final int FIELD_COUNT = 12;
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private PilotCreateFlightPlanPayload() {}

    public record Fields(
            String routeName,
            String aircraftRegistration,
            String pilotUsername,
            String departure,
            String arrival,
            double fuelValue,
            String fuelUnit,
            int passengerCount,
            double passengerWeightKg,
            double cargoWeightKg,
            String suffix,
            boolean confirmReplace) {}

    public static String encode(final Fields fields) {
        if (fields == null) {
            throw new IllegalArgumentException("fields are required.");
        }
        return String.join(";",
                require(fields.routeName(), "routeName"),
                require(fields.aircraftRegistration(), "aircraftRegistration"),
                require(fields.pilotUsername(), "pilotUsername"),
                require(fields.departure(), "departure"),
                require(fields.arrival(), "arrival"),
                Double.toString(fields.fuelValue()),
                require(fields.fuelUnit(), "fuelUnit"),
                Integer.toString(fields.passengerCount()),
                Double.toString(fields.passengerWeightKg()),
                Double.toString(fields.cargoWeightKg()),
                fields.suffix() != null ? fields.suffix().trim() : "",
                Boolean.toString(fields.confirmReplace()));
    }

    public static Fields decode(final String payload) {
        final String[] parts = splitPayload(payload);
        final String departure = parts[3].trim();
        final String arrival = parts[4].trim();
        parseDateTime(departure, "departure");
        parseDateTime(arrival, "arrival");
        final double fuelValue = parseDouble(parts[5].trim(), "fuelValue");
        final String fuelUnit = require(parts[6], "fuelUnit");
        final int passengerCount = parseInt(parts[7].trim(), "passengerCount");
        final double passengerWeightKg = parseDouble(parts[8].trim(), "passengerWeightKg");
        final double cargoWeightKg = parseDouble(parts[9].trim(), "cargoWeightKg");
        return new Fields(
                require(parts[0], "routeName"),
                require(parts[1], "aircraftRegistration"),
                require(parts[2], "pilotUsername"),
                departure,
                arrival,
                fuelValue,
                fuelUnit,
                passengerCount,
                passengerWeightKg,
                cargoWeightKg,
                parts[10].trim(),
                parseBoolean(parts[11].trim(), "confirmReplace"));
    }

    private static String[] splitPayload(final String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Expected: routeName;aircraftReg;pilotUsername;departure;arrival;"
                            + "fuelValue;fuelUnit;paxCount;paxWeightKg;cargoWeightKg;suffix;confirmReplace");
        }
        final String[] parts = payload.split(";", -1);
        if (parts.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected 12 semicolon-separated fields for CREATE_FLIGHT_PLAN.");
        }
        return parts;
    }

    private static String require(final String value, final String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    private static void parseDateTime(final String value, final String field) {
        try {
            LocalDateTime.parse(value, DATE_TIME);
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + field + " datetime: " + value);
        }
    }

    private static double parseDouble(final String value, final String field) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + value);
        }
    }

    private static int parseInt(final String value, final String field) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + value);
        }
    }

    private static boolean parseBoolean(final String value, final String field) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid " + field + ": " + value);
    }
}
