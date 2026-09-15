package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public final class FlightPlanAreaClipper {

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public FlightPlanDescriptor clip(final FlightPlanDescriptor source, final AreaCrossingSpan span) {
        if (source == null || span == null) {
            throw new IllegalArgumentException("Source plan and crossing span are required.");
        }
        if (span.legIndex() < 0 || span.legIndex() >= source.getLegs().size()) {
            throw new IllegalArgumentException("Crossing span leg index out of range.");
        }
        if (span.isFullLeg()) {
            return source;
        }

        final LegDescriptor leg = source.getLegs().get(span.legIndex());
        final LocalDateTime dep = LocalDateTime.parse(leg.getDepartureTime(), DT);
        final LocalDateTime arr = LocalDateTime.parse(leg.getArrivalTime(), DT);
        final long totalSeconds = Math.max(1, java.time.Duration.between(dep, arr).getSeconds());

        final long clippedStartSec = Math.round(totalSeconds * span.entryFraction());
        final long clippedEndSec = Math.round(totalSeconds * span.exitFraction());
        final LocalDateTime clippedDep = dep.plusSeconds(clippedStartSec);
        final LocalDateTime clippedArr = dep.plusSeconds(Math.max(clippedStartSec + 1, clippedEndSec));

        final double fuelScale = span.spanFraction();
        final double clippedFuel = leg.getFuelValue() * fuelScale;

        final List<SegmentDescriptor> clippedSegments = clipSegments(
                leg.getRoute().segments(), span.entryFraction(), span.exitFraction());

        final String depCode = span.entryFraction() <= 1e-6
                ? leg.getDepartureAirport()
                : entryCode();
        final String arrCode = span.exitFraction() >= 1.0 - 1e-6
                ? leg.getArrivalAirport()
                : exitCode();

        final LegDescriptor clippedLeg = new LegDescriptor(
                depCode,
                clippedDep.format(DT),
                arrCode,
                clippedArr.format(DT),
                new RouteDescriptor(clippedSegments),
                clippedFuel,
                leg.getFuelUnit());

        final List<LegDescriptor> legs = new ArrayList<>(source.getLegs());
        legs.set(span.legIndex(), clippedLeg);
        return new FlightPlanDescriptor(
                source.getFlightId(),
                source.getFlightType(),
                source.getPassengersCount(),
                source.getPassengerWeightKg() * fuelScale,
                source.getCargoWeightKg() * fuelScale,
                legs);
    }

    public Airport syntheticAirport(final String iata, final String icao,
                                    final double lat, final double lon, final String areaCode) {
        return new Airport(
                AirportIATACode.valueOf(iata),
                AirportICAOCode.valueOf(icao),
                Coordinates.valueOf(lat, lon, 100.0),
                AreaCode.valueOf(areaCode));
    }

    private static String entryCode() {
        return "ENT";
    }

    private static String exitCode() {
        return "EXT";
    }

    private static List<SegmentDescriptor> clipSegments(final List<SegmentDescriptor> segments,
                                                        final double entryFraction,
                                                        final double exitFraction) {
        if (segments.isEmpty()) {
            return List.of();
        }

        final double[] cumulative = cumulativeFractions(segments);
        final List<SegmentDescriptor> clipped = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            final SegmentDescriptor seg = segments.get(i);
            final double segStart = cumulative[i];
            final double segEnd = cumulative[i + 1];
            if (segEnd <= entryFraction || segStart >= exitFraction) {
                continue;
            }

            final double clipStart = Math.max(segStart, entryFraction);
            final double clipEnd = Math.min(segEnd, exitFraction);
            final double localStart = (clipStart - segStart) / (segEnd - segStart);
            final double localEnd = (clipEnd - segStart) / (segEnd - segStart);

            final double startLat = lerp(seg.startLatitude(), seg.endLatitude(), localStart);
            final double startLon = lerp(seg.startLongitude(), seg.endLongitude(), localStart);
            final double endLat = lerp(seg.startLatitude(), seg.endLatitude(), localEnd);
            final double endLon = lerp(seg.startLongitude(), seg.endLongitude(), localEnd);

            clipped.add(new SegmentDescriptor(
                    startLat, startLon, endLat, endLon,
                    seg.altitudeSlots(),
                    seg.windDirectionDegrees(),
                    seg.windSpeedMetresPerSecond()));
        }

        if (clipped.isEmpty()) {
            final SegmentDescriptor first = segments.get(0);
            clipped.add(new SegmentDescriptor(
                    first.startLatitude(), first.startLongitude(),
                    first.endLatitude(), first.endLongitude(),
                    first.altitudeSlots(),
                    first.windDirectionDegrees(),
                    first.windSpeedMetresPerSecond()));
        }
        return clipped;
    }

    private static double[] cumulativeFractions(final List<SegmentDescriptor> segments) {
        final double[] cumulative = new double[segments.size() + 1];
        double total = 0.0;
        final double[] lengths = new double[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            lengths[i] = segmentLength(segments.get(i));
            total += lengths[i];
        }
        cumulative[0] = 0.0;
        double sum = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            sum += lengths[i];
            cumulative[i + 1] = total <= 0.0 ? 1.0 : sum / total;
        }
        return cumulative;
    }

    private static double segmentLength(final SegmentDescriptor segment) {
        return Math.hypot(
                segment.endLatitude() - segment.startLatitude(),
                segment.endLongitude() - segment.startLongitude());
    }

    private static double lerp(final double a, final double b, final double t) {
        return a + t * (b - a);
    }
}
