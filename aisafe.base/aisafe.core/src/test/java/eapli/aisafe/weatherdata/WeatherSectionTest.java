package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.weatherdata.domain.WeatherSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherSectionTest {

    @Test
    void testConstructorRejectsNullBoundary() {
        assertThrows(IllegalArgumentException.class, () -> WeatherSection.valueOf(null));
    }

    @Test
    void testGetGeoBound() {
        GeographicBoundary boundary = GeographicBoundary.valueOf(List.of(
                GeographicCoords.valueOf(0, 0),
                GeographicCoords.valueOf(5, 0),
                GeographicCoords.valueOf(0, 5)
        ));

        WeatherSection section = WeatherSection.valueOf(boundary);
        assertEquals(boundary, section.getGeoBound());
    }

    @Test
    void testProtectedConstructorCoverage() {
        class ProtectedSection extends WeatherSection { ProtectedSection() { super(); } }
        assertNotNull(new ProtectedSection());
    }
}
