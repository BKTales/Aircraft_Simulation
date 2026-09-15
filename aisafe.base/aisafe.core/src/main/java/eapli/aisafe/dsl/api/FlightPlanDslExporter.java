package eapli.aisafe.dsl.api;

import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;

import java.util.Locale;

/**
 * Serializes a {@link FlightPlanDescriptor} to Core Flight DSL text ({@code FlightPlan.g4}).
 */
public final class FlightPlanDslExporter {

    private FlightPlanDslExporter() {}

    public static String toDsl(final FlightPlanDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("descriptor is required.");
        }
        final StringBuilder sb = new StringBuilder(2048);
        sb.append("flight ").append(descriptor.getFlightId()).append(" {\n");
        sb.append("    type ").append(descriptor.getFlightType()).append(" ;\n");
        sb.append("    load ")
                .append("passengers ")
                .append(descriptor.getPassengersCount())
                .append(" pax_weight ")
                .append(formatDecimal(descriptor.getPassengerWeightKg()))
                .append(" kg ")
                .append("cargo_weight ")
                .append(formatDecimal(descriptor.getCargoWeightKg()))
                .append(" kg ;\n\n");

        for (final LegDescriptor leg : descriptor.getLegs()) {
            appendLeg(sb, leg);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void appendLeg(final StringBuilder sb, final LegDescriptor leg) {
        sb.append("    leg {\n");
        sb.append("        departure ").append(leg.getDepartureAirport()).append(' ')
                .append(leg.getDepartureTime()).append(" ;\n");
        sb.append("        arrival ").append(leg.getArrivalAirport()).append(' ')
                .append(leg.getArrivalTime()).append(" ;\n\n");

        appendRoute(sb, leg.getRoute());

        sb.append("\n        fuel ").append(formatDecimal(leg.getFuelValue())).append(' ')
                .append(normalizeMassUnit(leg.getFuelUnit())).append(" ;\n");
        sb.append("    }\n");
    }

    private static void appendRoute(final StringBuilder sb, final RouteDescriptor route) {
        sb.append("        route {\n");
        if (route != null) {
            for (final SegmentDescriptor segment : route.segments()) {
                appendSegment(sb, segment);
            }
        }
        sb.append("        }\n");
    }

    private static void appendSegment(final StringBuilder sb, final SegmentDescriptor segment) {
        sb.append("            segment (")
                .append(formatCoord(segment.startLatitude())).append(", ")
                .append(formatCoord(segment.startLongitude())).append(") (")
                .append(formatCoord(segment.endLatitude())).append(", ")
                .append(formatCoord(segment.endLongitude())).append(")\n");

        for (final AltitudeSlotDescriptor slot : segment.altitudeSlots()) {
            sb.append("                alt ").append(slot.altitudeMetres()).append(" m width ")
                    .append(slot.widthMetres()).append(" m ;\n");
        }

        sb.append("                wind ").append(segment.windDirectionDegrees()).append(' ')
                .append(formatDecimal(segment.windSpeedMetresPerSecond())).append(" m/s ;\n");
    }

    private static String normalizeMassUnit(final String unit) {
        if (unit == null || unit.isBlank()) {
            return "kg";
        }
        final String normalized = unit.trim().toLowerCase(Locale.ROOT);
        return "l".equals(normalized) ? "l" : "kg";
    }

    private static String formatCoord(final double value) {
        return trimDouble(value);
    }

    private static String formatDecimal(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0.0";
        }
        final long asLong = (long) value;
        if (value == asLong) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return trimDouble(value);
    }

    private static String trimDouble(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        final long asLong = (long) value;
        return (value == asLong) ? Long.toString(asLong) : Double.toString(value);
    }
}
