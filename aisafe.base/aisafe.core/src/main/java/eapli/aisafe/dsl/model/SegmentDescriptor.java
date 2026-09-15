package eapli.aisafe.dsl.model;

import java.util.Collections;
import java.util.List;

public final class SegmentDescriptor {

    private final double startLatitude;
    private final double startLongitude;
    private final double endLatitude;
    private final double endLongitude;
    private final List<AltitudeSlotDescriptor> altitudeSlots;
    private final int windDirectionDegrees;
    private final double windSpeedMetresPerSecond;

    public SegmentDescriptor(final double startLatitude, final double startLongitude,
                             final double endLatitude, final double endLongitude,
                             final List<AltitudeSlotDescriptor> altitudeSlots,
                             final int windDirectionDegrees, final double windSpeedMetresPerSecond) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.altitudeSlots = List.copyOf(altitudeSlots);
        this.windDirectionDegrees = windDirectionDegrees;
        this.windSpeedMetresPerSecond = windSpeedMetresPerSecond;
    }

    public double startLatitude() {
        return startLatitude;
    }

    public double startLongitude() {
        return startLongitude;
    }

    public double endLatitude() {
        return endLatitude;
    }

    public double endLongitude() {
        return endLongitude;
    }

    public List<AltitudeSlotDescriptor> altitudeSlots() {
        return Collections.unmodifiableList(altitudeSlots);
    }

    public int windDirectionDegrees() {
        return windDirectionDegrees;
    }

    public double windSpeedMetresPerSecond() {
        return windSpeedMetresPerSecond;
    }
}

