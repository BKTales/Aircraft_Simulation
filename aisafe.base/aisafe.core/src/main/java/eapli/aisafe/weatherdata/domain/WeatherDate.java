package eapli.aisafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class WeatherDate implements ValueObject, Comparable<WeatherDate> {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    protected WeatherDate() {} // JPA

    public WeatherDate(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.startDateTime = start;
        this.endDateTime = end;
    }

    public static WeatherDate valueOf(final LocalDateTime start, final LocalDateTime end) {
        return new WeatherDate(start, end);
    }

    public boolean isNow() {
        LocalDateTime now = LocalDateTime.now();
        return (now.isAfter(startDateTime) && now.isBefore(endDateTime));
    }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherDate)) return false;
        WeatherDate that = (WeatherDate) o;
        return Objects.equals(startDateTime, that.startDateTime) &&
                Objects.equals(endDateTime, that.endDateTime);
    }

    @Override
    public int hashCode() { return Objects.hash(startDateTime, endDateTime); }

    @Override
    public int compareTo(WeatherDate other) {
        return this.startDateTime.compareTo(other.startDateTime);
    }
}
