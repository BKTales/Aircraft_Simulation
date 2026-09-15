package eapli.aisafe.flightmanagement.infrastructure.geography;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RouteAreaCrossingDetector {

    private static final int SAMPLES_PER_SEGMENT = 40;

    public boolean crossesArea(final FlightPlanDescriptor descriptor, final GeographicBoundary boundary) {
        return findCrossingSpan(descriptor, boundary).isPresent();
    }

    public Optional<AreaCrossingSpan> findCrossingSpan(final FlightPlanDescriptor descriptor,
                                                       final GeographicBoundary boundary) {
        if (descriptor == null || boundary == null) {
            return Optional.empty();
        }
        for (int legIndex = 0; legIndex < descriptor.getLegs().size(); legIndex++) {
            final Optional<AreaCrossingSpan> span = findLegCrossingSpan(
                    legIndex, descriptor.getLegs().get(legIndex), boundary);
            if (span.isPresent()) {
                return span;
            }
        }
        return Optional.empty();
    }

    private Optional<AreaCrossingSpan> findLegCrossingSpan(final int legIndex,
                                                           final LegDescriptor leg,
                                                           final GeographicBoundary boundary) {
        final List<PathPoint> path = buildLegPath(leg);
        if (path.size() < 2) {
            return Optional.empty();
        }

        final boolean startInside = boundary.contains(
                GeographicCoords.valueOf(path.get(0).latitude(), path.get(0).longitude()));
        final boolean endInside = boundary.contains(
                GeographicCoords.valueOf(path.get(path.size() - 1).latitude(), path.get(path.size() - 1).longitude()));

        final List<InsideRange> ranges = collectInsideRanges(path, boundary);
        if (ranges.isEmpty()) {
            return Optional.empty();
        }

        final double entryFraction = startInside ? 0.0 : ranges.get(0).startFraction();
        final double exitFraction = endInside ? 1.0 : ranges.get(ranges.size() - 1).endFraction();
        final PathPoint entry = interpolate(path, entryFraction);
        final PathPoint exit = interpolate(path, exitFraction);

        return Optional.of(new AreaCrossingSpan(
                legIndex,
                entryFraction,
                exitFraction,
                entry.latitude(),
                entry.longitude(),
                exit.latitude(),
                exit.longitude()));
    }

    private static List<InsideRange> collectInsideRanges(final List<PathPoint> path,
                                                         final GeographicBoundary boundary) {
        final List<InsideRange> ranges = new ArrayList<>();
        Boolean inside = null;
        double rangeStart = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            final PathPoint a = path.get(i);
            final PathPoint b = path.get(i + 1);
            for (int s = 0; s <= SAMPLES_PER_SEGMENT; s++) {
                final double t = s / (double) SAMPLES_PER_SEGMENT;
                final double lat = a.latitude() + t * (b.latitude() - a.latitude());
                final double lon = a.longitude() + t * (b.longitude() - a.longitude());
                final double fraction = a.fraction() + t * (b.fraction() - a.fraction());
                final boolean pointInside = boundary.contains(GeographicCoords.valueOf(lat, lon));

                if (inside == null) {
                    inside = pointInside;
                    rangeStart = fraction;
                } else if (pointInside != inside) {
                    if (inside) {
                        ranges.add(new InsideRange(rangeStart, fraction));
                    }
                    inside = pointInside;
                    rangeStart = fraction;
                }
            }
        }

        if (Boolean.TRUE.equals(inside)) {
            ranges.add(new InsideRange(rangeStart, 1.0));
        }
        return ranges;
    }

    private static List<PathPoint> buildLegPath(final LegDescriptor leg) {
        final RouteDescriptor route = leg.getRoute();
        if (route == null || route.segments().isEmpty()) {
            return List.of();
        }

        final List<SegmentDescriptor> segments = route.segments();
        final double[] cumulative = cumulativeDistances(segments);
        final double total = cumulative[cumulative.length - 1];
        if (total <= 0.0) {
            return List.of();
        }

        final List<PathPoint> path = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            final SegmentDescriptor seg = segments.get(i);
            final double startFrac = cumulative[i] / total;
            path.add(new PathPoint(startFrac, seg.startLatitude(), seg.startLongitude()));
            if (i == segments.size() - 1) {
                path.add(new PathPoint(1.0, seg.endLatitude(), seg.endLongitude()));
            }
        }
        return path;
    }

    private static double[] cumulativeDistances(final List<SegmentDescriptor> segments) {
        final double[] cumulative = new double[segments.size() + 1];
        double sum = 0.0;
        cumulative[0] = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            sum += segmentLength(segments.get(i));
            cumulative[i + 1] = sum;
        }
        return cumulative;
    }

    private static double segmentLength(final SegmentDescriptor segment) {
        final double dLat = segment.endLatitude() - segment.startLatitude();
        final double dLon = segment.endLongitude() - segment.startLongitude();
        return Math.hypot(dLat, dLon);
    }

    private static PathPoint interpolate(final List<PathPoint> path, final double fraction) {
        final double f = Math.max(0.0, Math.min(1.0, fraction));
        for (int i = 0; i < path.size() - 1; i++) {
            final PathPoint a = path.get(i);
            final PathPoint b = path.get(i + 1);
            if (f >= a.fraction() && f <= b.fraction()) {
                final double span = b.fraction() - a.fraction();
                final double t = span <= 0.0 ? 0.0 : (f - a.fraction()) / span;
                return new PathPoint(
                        f,
                        a.latitude() + t * (b.latitude() - a.latitude()),
                        a.longitude() + t * (b.longitude() - a.longitude()));
            }
        }
        final PathPoint last = path.get(path.size() - 1);
        return new PathPoint(f, last.latitude(), last.longitude());
    }

    private record PathPoint(double fraction, double latitude, double longitude) {}

    private record InsideRange(double startFraction, double endFraction) {}
}
