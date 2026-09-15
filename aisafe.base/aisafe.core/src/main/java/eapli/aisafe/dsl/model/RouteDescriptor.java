package eapli.aisafe.dsl.model;

import java.util.Collections;
import java.util.List;

public final class RouteDescriptor {

    private final List<SegmentDescriptor> segments;

    public RouteDescriptor(final List<SegmentDescriptor> segments) {
        this.segments = List.copyOf(segments);
    }

    public List<SegmentDescriptor> segments() {
        return Collections.unmodifiableList(segments);
    }
}

