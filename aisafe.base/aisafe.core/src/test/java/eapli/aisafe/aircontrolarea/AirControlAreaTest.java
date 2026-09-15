package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.exceptions.NegativeFuelException;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.weatherdata.application.WeatherComplianceService;
import eapli.aisafe.weatherdata.application.exceptions.WeatherSectionOutOfBoundsException;
import eapli.aisafe.weatherdata.domain.*;
import eapli.aisafe.weatherdata.domain.winddata.WindData;
import eapli.aisafe.weatherdata.domain.winddata.WindDataDirection;
import eapli.aisafe.weatherdata.domain.winddata.WindDataSpeed;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AirControlAreaTest {

    @Test
    public void testAirControlAreaCreationAndIdentity() {
        AirControlAreaName name = AirControlAreaName.valueOf("Sector 7");
        List<GeographicCoords> coords = Arrays.asList(
                GeographicCoords.valueOf(0,0), GeographicCoords.valueOf(5,0), GeographicCoords.valueOf(0,5));
        GeographicBoundary boundary = GeographicBoundary.valueOf(coords);
        MinFuelRequirement fuel = MinFuelRequirement.valueOf(500);

        AirControlArea area = new AirControlArea(name, boundary, fuel);

        assertNotNull(area.identity());
        assertEquals(name, area.getName());
        assertEquals(fuel, area.getMinFuelRequirement());
        assertTrue(area.sameAs(area));
        assertFalse(area.sameAs(null));
    }

    @Test
    public void testMinFuelRequirementException() {
        assertThrows(NegativeFuelException.class, () -> MinFuelRequirement.valueOf(-1));
    }

    @Test
    public void testAirControlAreaNameTechnical() {
        AirControlAreaName name = AirControlAreaName.valueOf("Lisboa");

        assertEquals("Lisboa", name.getName());

        class ProtectedName extends AirControlAreaName {
            public ProtectedName() { super(); }
        }
        assertNotNull(new ProtectedName());
    }

    @Test
    public void testAirControlAreaTechnicalCoverage() {
        AirControlArea protectedArea = new AirControlArea() {};
        assertNotNull(protectedArea);

        AirControlAreaName name = AirControlAreaName.valueOf("Test Zone");
        List<GeographicCoords> coords = Arrays.asList(
                GeographicCoords.valueOf(0,0), GeographicCoords.valueOf(10,0), GeographicCoords.valueOf(0,10));
        GeographicBoundary boundary = GeographicBoundary.valueOf(coords);
        MinFuelRequirement fuel = MinFuelRequirement.valueOf(300);

        AirControlArea area = new AirControlArea(name, boundary, fuel);

        assertNotNull(area.getGeographicBoundary());
        assertNotNull(area.getAreaCode());
        assertNotNull(area.getName());
        assertNotNull(area.getMinFuelRequirement());

        assertNotNull(area.identity());
        assertTrue(area.sameAs(area));
        assertFalse(area.sameAs(null));
        assertFalse(area.sameAs(new Object()));
    }

    @Test
    public void addWeatherSectionAddsWhenSectionIsInsideArea() {
        final AirControlArea area = new AirControlArea(
                AirControlAreaName.valueOf("Sector WX"),
                GeographicBoundary.valueOf(Arrays.asList(
                        GeographicCoords.valueOf(0, 0),
                        GeographicCoords.valueOf(10, 0),
                        GeographicCoords.valueOf(10, 10),
                        GeographicCoords.valueOf(0, 10)
                )),
                MinFuelRequirement.valueOf(200)
        );

        final WeatherData weather = new WeatherData(
                WeatherDate.valueOf(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(10)),
                Humidity.valueOf(50),
                Pressure.valueOf(1013),
                Temperature.valueOf(22),
                WindData.valueOf(WindDataDirection.valueOf(180), WindDataSpeed.valueOf(10)),
                WeatherSection.valueOf(GeographicBoundary.valueOf(Arrays.asList(
                        GeographicCoords.valueOf(1, 1),
                        GeographicCoords.valueOf(2, 1),
                        GeographicCoords.valueOf(1, 2)
                ))),
                area
        );

        assertDoesNotThrow(() -> area.addWeatherSection(weather, new WeatherComplianceService()));
    }

    @Test
    public void addWeatherSectionThrowsWhenSectionIsOutsideArea() {
        final AirControlArea area = new AirControlArea(
                AirControlAreaName.valueOf("Sector WX"),
                GeographicBoundary.valueOf(Arrays.asList(
                        GeographicCoords.valueOf(0, 0),
                        GeographicCoords.valueOf(10, 0),
                        GeographicCoords.valueOf(10, 10),
                        GeographicCoords.valueOf(0, 10)
                )),
                MinFuelRequirement.valueOf(200)
        );

        final WeatherData weather = new WeatherData(
                WeatherDate.valueOf(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(10)),
                Humidity.valueOf(50),
                Pressure.valueOf(1013),
                Temperature.valueOf(22),
                WindData.valueOf(WindDataDirection.valueOf(180), WindDataSpeed.valueOf(10)),
                WeatherSection.valueOf(GeographicBoundary.valueOf(Arrays.asList(
                        GeographicCoords.valueOf(20, 20),
                        GeographicCoords.valueOf(21, 20),
                        GeographicCoords.valueOf(20, 21)
                ))),
                area
        );

        assertThrows(WeatherSectionOutOfBoundsException.class,
                () -> area.addWeatherSection(weather, new WeatherComplianceService()));
    }

    @Test
    public void testAreaCodeLogic() {
        AreaCode code1 = new AreaCode();
        AreaCode code2 = new AreaCode();
        assertNotEquals(code1, code2);
        assertEquals(0, code1.compareTo(code1));
        assertNotNull(code1.toString());
        assertNotNull(code1.hashCode());
    }
}
