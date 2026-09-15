package eapli.aisafe.dsl.model;

import eapli.aisafe.dsl.parse.FlightPlanSourceIndex;
import eapli.aisafe.dsl.parse.SemanticErrorFormatter;
import eapli.aisafe.dsl.parse.SourceSpan;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlightPlanDescriptor {

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final String flightId;
    private final String flightType;
    private final int passengersCount;
    private final double passengerWeightKg;
    private final double cargoWeightKg;
    private final List<LegDescriptor> legs;

    public FlightPlanDescriptor(final String flightId,
                                final String flightType,
                                final int passengersCount,
                                final double loadWeight,
                                final List<LegDescriptor> legs) {
        this(flightId, flightType, passengersCount, loadWeight, 0.0, legs);
    }

    public FlightPlanDescriptor(final String flightId,
                                final String flightType,
                                final int passengersCount,
                                final double passengerWeightKg,
                                final double cargoWeightKg,
                                final List<LegDescriptor> legs) {
        this.flightId = flightId;
        this.flightType = flightType;
        this.passengersCount = passengersCount;
        this.passengerWeightKg = passengerWeightKg;
        this.cargoWeightKg = cargoWeightKg;
        this.legs = legs;
    }

    public String getFlightId()           { return flightId; }
    public String getFlightType()         { return flightType; }
    public int getPassengersCount()          { return passengersCount; }
    public double getPassengerWeightKg() { return passengerWeightKg; }
    public double getCargoWeightKg()     { return cargoWeightKg; }
    public double getLoadWeight()         { return passengerWeightKg + cargoWeightKg; }
    public List<LegDescriptor> getLegs()  { return legs; }

    public List<String> validateSemantics() {
        return validateSemantics(null);
    }

    public List<String> validateSemantics(final FlightPlanSourceIndex sources) {
        List<String> errors = new ArrayList<>();

        if (flightType == null || (!"REGULAR".equals(flightType) && !"CHARTER".equals(flightType))) {
            SourceSpan span = sources != null ? sources.flightType() : null;
            SemanticErrorFormatter.add(errors, span, "Flight: Type must be REGULAR or CHARTER.");
        }

        if (passengersCount < 0 || passengerWeightKg < 0 || cargoWeightKg < 0) {
            SourceSpan span = sources != null ? sources.load() : null;
            SemanticErrorFormatter.add(errors, span,
                    "Flight: Load quantity and weight cannot be negative.");
        }

        Set<String> visitedAirports = new HashSet<>();

        for (int i = 0; i < legs.size(); i++) {
            LegDescriptor current = legs.get(i);
            FlightPlanSourceIndex.LegSource legSource = legSpan(sources, i);

            LocalDateTime dep = parseDateTime(errors, legSource, i + 1, "Departure",
                    current.getDepartureTime(), legSource != null ? legSource.departure : null);
            LocalDateTime arr = parseDateTime(errors, legSource, i + 1, "Arrival",
                    current.getArrivalTime(), legSource != null ? legSource.arrival : null);

            if (dep != null && arr != null && !arr.isAfter(dep)) {
                SemanticErrorFormatter.add(errors, legSource != null ? legSource.arrival : null,
                        "Leg " + (i + 1) + ": Arrival time (" + current.getArrivalTime()
                                + ") must be after Departure time (" + current.getDepartureTime() + ").");
            }

            if (i > 0) {
                LegDescriptor previous = legs.get(i - 1);
                if (!previous.getArrivalAirport().equals(current.getDepartureAirport())) {
                    SemanticErrorFormatter.add(errors, legSource != null ? legSource.departure : null,
                            "Leg " + (i + 1) + ": Route discontinuity. Departure airport ("
                                    + current.getDepartureAirport() + ") does not match previous arrival ("
                                    + previous.getArrivalAirport() + ").");
                }

                LocalDateTime prevArr = parseDateTime(errors, legSpan(sources, i - 1), i, "Arrival",
                        previous.getArrivalTime(),
                        legSpan(sources, i - 1) != null ? legSpan(sources, i - 1).arrival : null);
                if (prevArr != null && dep != null && !prevArr.isBefore(dep)) {
                    SemanticErrorFormatter.add(errors, legSource != null ? legSource.departure : null,
                            "Leg " + (i + 1) + ": Departure time (" + current.getDepartureTime()
                                    + ") must be after previous arrival (" + previous.getArrivalTime() + ").");
                }
            }

            if (current.getFuelValue() <= 0) {
                SemanticErrorFormatter.add(errors, legSource != null ? legSource.fuel : null,
                        "Leg " + (i + 1) + ": Fuel amount must be greater than zero.");
            }

            if (i == 0) {
                if (!visitedAirports.add(current.getDepartureAirport())) {
                    SemanticErrorFormatter.add(errors, legSource != null ? legSource.departure : null,
                            "Flight: Airport repeated (" + current.getDepartureAirport() + ").");
                }
            }
            if (!visitedAirports.add(current.getArrivalAirport())) {
                SemanticErrorFormatter.add(errors, legSource != null ? legSource.arrival : null,
                        "Flight: Airport repeated (" + current.getArrivalAirport() + ").");
            }

            validateRoute(errors, i + 1, current.getRoute(), legSource);
        }

        return errors;
    }

    private static FlightPlanSourceIndex.LegSource legSpan(final FlightPlanSourceIndex sources, final int legIndex) {
        if (sources == null || legIndex >= sources.legs().size()) {
            return null;
        }
        return sources.legs().get(legIndex);
    }

    private static LocalDateTime parseDateTime(final List<String> errors,
                                               final FlightPlanSourceIndex.LegSource legSource,
                                               final int legNumber,
                                               final String field,
                                               final String value,
                                               final SourceSpan span) {
        try {
            return LocalDateTime.parse(value, DT);
        } catch (Exception e) {
            SemanticErrorFormatter.add(errors, span,
                    "Leg " + legNumber + ": Invalid " + field + " datetime (" + value + ").");
            return null;
        }
    }

    private static void validateRoute(final List<String> errors,
                                      final int legNumber,
                                      final RouteDescriptor route,
                                      final FlightPlanSourceIndex.LegSource legSource) {
        if (route == null) {
            SemanticErrorFormatter.add(errors, null, "Leg " + legNumber + ": Route is missing.");
            return;
        }
        int segIdx = 0;
        for (SegmentDescriptor s : route.segments()) {
            segIdx++;
            FlightPlanSourceIndex.SegmentSource segmentSource = segmentSpan(legSource, segIdx - 1);
            SourceSpan segmentAt = segmentSource != null ? segmentSource.segment : null;
            SourceSpan windAt = segmentSource != null ? segmentSource.wind : null;

            if (Double.compare(s.startLatitude(), s.endLatitude()) == 0 &&
                    Double.compare(s.startLongitude(), s.endLongitude()) == 0) {
                SemanticErrorFormatter.add(errors, segmentAt,
                        "Leg " + legNumber + ", Segment " + segIdx + ": Start and end coordinates must differ.");
            }
            if (!inRange(s.startLatitude(), -90, 90) || !inRange(s.endLatitude(), -90, 90) ||
                    !inRange(s.startLongitude(), -180, 180) || !inRange(s.endLongitude(), -180, 180)) {
                SemanticErrorFormatter.add(errors, segmentAt,
                        "Leg " + legNumber + ", Segment " + segIdx + ": Coordinates out of range.");
            }
            if (s.windDirectionDegrees() < 0 || s.windDirectionDegrees() > 360) {
                SemanticErrorFormatter.add(errors, windAt,
                        "Leg " + legNumber + ", Segment " + segIdx + ": Wind direction must be between 0 and 360.");
            }
            if (s.windSpeedMetresPerSecond() <= 0) {
                SemanticErrorFormatter.add(errors, windAt,
                        "Leg " + legNumber + ", Segment " + segIdx + ": Wind speed must be greater than zero.");
            }

            int slotIdx = 0;
            for (AltitudeSlotDescriptor slot : s.altitudeSlots()) {
                slotIdx++;
                if (slot.altitudeMetres() <= 0) {
                    SemanticErrorFormatter.add(errors, segmentAt,
                            "Leg " + legNumber + ", Segment " + segIdx + ", AltSlot " + slotIdx
                                    + ": Altitude must be > 0.");
                }
                if (slot.widthMetres() <= 0) {
                    SemanticErrorFormatter.add(errors, segmentAt,
                            "Leg " + legNumber + ", Segment " + segIdx + ", AltSlot " + slotIdx
                                    + ": Width must be > 0.");
                }
            }
        }
    }

    private static FlightPlanSourceIndex.SegmentSource segmentSpan(
            final FlightPlanSourceIndex.LegSource legSource, final int segmentIndex) {
        if (legSource == null || segmentIndex >= legSource.segments.size()) {
            return null;
        }
        return legSource.segments.get(segmentIndex);
    }

    private static boolean inRange(final double v, final double min, final double max) {
        return v >= min && v <= max;
    }

    @Override
    public String toString() {
        return "FlightPlanDescriptor{id=" + flightId + ", type=" + flightType
                + ", legs=" + legs.size() + "}";
    }
}
