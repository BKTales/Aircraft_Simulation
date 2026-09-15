package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.domain.WeatherDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDateTest {

    @Test
    void testConstructorRejectsNullDates() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> WeatherDate.valueOf(null, now));
        assertThrows(IllegalArgumentException.class, () -> WeatherDate.valueOf(now, null));
    }

    @Test
    void testConstructorRejectsEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusMinutes(1);

        assertThrows(IllegalArgumentException.class, () -> WeatherDate.valueOf(start, end));
    }

    @Test
    void testIsNowTrueAndFalseCases() {
        WeatherDate current = WeatherDate.valueOf(LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(5));
        WeatherDate past = WeatherDate.valueOf(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));

        assertTrue(current.isNow());
        assertFalse(past.isNow());
    }

    @Test
    void testEqualsHashCodeAndCompareTo() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        WeatherDate a = WeatherDate.valueOf(start, end);
        WeatherDate b = WeatherDate.valueOf(start, end);
        WeatherDate later = WeatherDate.valueOf(start.plusHours(1), end.plusHours(1));

        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, later);
        assertNotEquals(a, null);
        assertNotEquals(a, "weather-date");

        assertEquals(0, a.compareTo(b));
        assertTrue(a.compareTo(later) < 0);
        assertTrue(later.compareTo(a) > 0);
    }

    @Test
    void testGettersAndProtectedConstructor() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);
        WeatherDate date = WeatherDate.valueOf(start, end);

        assertEquals(start, date.getStartDateTime());
        assertEquals(end, date.getEndDateTime());

        class ProtectedWeatherDate extends WeatherDate {
            public ProtectedWeatherDate() { super(); }
        }
        assertNotNull(new ProtectedWeatherDate());
    }

    @Test
    void testEqualsCoversAlternativeFalsePath() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        WeatherDate base = WeatherDate.valueOf(start, end);
        WeatherDate sameStartDifferentEnd = WeatherDate.valueOf(start, end.plusMinutes(1));

        assertNotEquals(base, sameStartDifferentEnd);
    }
}
