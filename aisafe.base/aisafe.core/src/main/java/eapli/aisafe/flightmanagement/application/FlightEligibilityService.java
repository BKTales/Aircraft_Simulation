package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.geography.AreaCrossingSpan;
import eapli.aisafe.flightmanagement.infrastructure.geography.RouteAreaCrossingDetector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FlightEligibilityService {

    private final AirControlAreaRepository areaRepository;
    private final FlightDslParser dslParser;
    private final RouteAreaCrossingDetector crossingDetector;

    public FlightEligibilityService() {
        this(null, new FlightDslParser(), new RouteAreaCrossingDetector());
    }

    public FlightEligibilityService(final AirControlAreaRepository areaRepository,
                                    final FlightDslParser dslParser,
                                    final RouteAreaCrossingDetector crossingDetector) {
        this.areaRepository = areaRepository;
        this.dslParser = Objects.requireNonNull(dslParser, "dslParser");
        this.crossingDetector = Objects.requireNonNull(crossingDetector, "crossingDetector");
    }

    public boolean isEligible(final Flight flight,
                              final String areaCode,
                              final LocalDateTime intervalStart,
                              final LocalDateTime intervalEnd) {
        Objects.requireNonNull(flight, "flight");
        validateInterval(areaCode, intervalStart, intervalEnd);
        return flight.hasPlanReadyForSimulation()
                && flight.scheduleOverlaps(intervalStart, intervalEnd)
                && resolveDescriptor(flight)
                        .flatMap(descriptor -> resolveBoundary(areaCode)
                                .map(boundary -> crossingDetector.crossesArea(descriptor, boundary)))
                        .orElse(false);
    }

    public Optional<AreaClipMode> clipModeFor(final Flight flight, final String areaCode) {
        return resolveDescriptor(flight)
                .flatMap(descriptor -> resolveBoundary(areaCode)
                        .flatMap(boundary -> crossingDetector.findCrossingSpan(descriptor, boundary))
                        .map(span -> span.isFullLeg() ? AreaClipMode.FULL : AreaClipMode.CLIPPED));
    }

    public Optional<AreaCrossingSpan> crossingSpanFor(final Flight flight, final String areaCode) {
        return resolveDescriptor(flight)
                .flatMap(descriptor -> resolveBoundary(areaCode)
                        .flatMap(boundary -> crossingDetector.findCrossingSpan(descriptor, boundary)));
    }

    public List<Flight> filterEligible(final List<Flight> candidates,
                                       final String areaCode,
                                       final LocalDateTime intervalStart,
                                       final LocalDateTime intervalEnd) {
        Objects.requireNonNull(candidates, "candidates");
        validateInterval(areaCode, intervalStart, intervalEnd);
        return candidates.stream()
                .filter(f -> isEligible(f, areaCode, intervalStart, intervalEnd))
                .toList();
    }

    private Optional<FlightPlanDescriptor> resolveDescriptor(final Flight flight) {
        if (flight.flightPlan() == null || !flight.flightPlan().hasDslContent()) {
            return Optional.empty();
        }
        final ParseResult parsed = dslParser.parse(flight.flightPlan().dslContent());
        if (!parsed.isValid() || parsed.getDescriptor() == null) {
            return Optional.empty();
        }
        return Optional.of(parsed.getDescriptor());
    }

    private Optional<GeographicBoundary> resolveBoundary(final String areaCode) {
        if (areaRepository == null) {
            return Optional.empty();
        }
        return areaRepository.ofIdentity(AreaCode.valueOf(areaCode))
                .map(area -> area.getGeographicBoundary());
    }

    private static void validateInterval(final String areaCode,
                                         final LocalDateTime intervalStart,
                                         final LocalDateTime intervalEnd) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required.");
        }
        if (intervalStart == null || intervalEnd == null || intervalEnd.isBefore(intervalStart)) {
            throw new IllegalArgumentException("Invalid simulation interval.");
        }
    }
}
