package eapli.aisafe.dsl.api;

import eapli.aisafe.airportmanagement.domain.Airport;

public final class SimulatorAirportJsonMapper {

    private SimulatorAirportJsonMapper() {}

    public static String toJsonObject(final Airport airport) {
        if (airport == null) {
            throw new IllegalArgumentException("Airport is required.");
        }
        final String iata = airport.identity().toString();
        final String icao = airport.icaoCode().toString();
        final double lat = airport.coordinates().latitude();
        final double lon = airport.coordinates().longitude();
        final double alt = airport.coordinates().elevationMeters();

        final StringBuilder sb = new StringBuilder(256);
        sb.append("{\n");
        appendField(sb, "Id", iata, true);
        appendField(sb, "Icao", icao, true);
        appendField(sb, "AreaCode", airport.airControlAreaCode(), true);
        appendField(sb, "Name", iata, true);
        appendField(sb, "Town", "", true);
        appendField(sb, "Country", "", true);
        appendNumber(sb, "Latitude", lat);
        appendNumber(sb, "Longitude", lon);
        appendNumberLast(sb, "Altitude", alt);
        sb.append("\n  }");
        return sb.toString();
    }

    private static void appendField(final StringBuilder sb, final String key, final String value, final boolean comma) {
        sb.append("      \"").append(key).append("\": \"").append(escape(value)).append("\"");
        if (comma) {
            sb.append(",\n");
        } else {
            sb.append('\n');
        }
    }

    private static void appendNumber(final StringBuilder sb, final String key, final double value) {
        sb.append("      \"").append(key).append("\": ").append(SimulatorAircraftJsonMapper.trim(value)).append(",\n");
    }

    private static void appendNumberLast(final StringBuilder sb, final String key, final double value) {
        sb.append("      \"").append(key).append("\": ").append(SimulatorAircraftJsonMapper.trim(value));
    }

    private static String escape(final String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
