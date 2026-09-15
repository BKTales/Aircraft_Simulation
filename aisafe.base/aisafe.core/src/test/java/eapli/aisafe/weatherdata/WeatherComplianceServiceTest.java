package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.domain.*;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.*;
import eapli.aisafe.weatherdata.application.WeatherComplianceService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherComplianceServiceTest {

    private AirControlArea buildSquareArea() {
        GeographicBoundary areaBound = new GeographicBoundary(List.of(
                new GeographicCoords(0, 0),
                new GeographicCoords(10, 0),
                new GeographicCoords(10, 10),
                new GeographicCoords(0, 10)
        ));
        return new AirControlArea(new AirControlAreaName("T"), areaBound, new MinFuelRequirement(1));
    }

    @Test
    void testPointInclusion() {
        WeatherComplianceService service = new WeatherComplianceService();
        AirControlArea area = buildSquareArea();

        GeographicBoundary inside = new GeographicBoundary(List.of(
                new GeographicCoords(1, 1), new GeographicCoords(2, 1), new GeographicCoords(1, 2)));

        GeographicBoundary outside = new GeographicBoundary(List.of(
                new GeographicCoords(15, 15), new GeographicCoords(16, 15), new GeographicCoords(15, 16)));

        assertTrue(service.isSectionValidInsideArea(area, inside));
        assertFalse(service.isSectionValidInsideArea(area, outside));
    }

    @Test
    void testEmptySectionIsInvalid() {
        WeatherComplianceService service = new WeatherComplianceService();
        AirControlArea area = buildSquareArea();

        GeographicBoundary emptySection = mock(GeographicBoundary.class);
        when(emptySection.getGeoCords()).thenReturn(List.of());

        assertFalse(service.isSectionValidInsideArea(area, emptySection));
    }

    @Test
    void testPointOnAreaVertexIsConsideredInside() {
        WeatherComplianceService service = new WeatherComplianceService();
        AirControlArea area = buildSquareArea();

        GeographicBoundary onVertex = new GeographicBoundary(List.of(
                new GeographicCoords(0, 0),
                new GeographicCoords(1, 1),
                new GeographicCoords(2, 1)
        ));

        assertTrue(service.isSectionValidInsideArea(area, onVertex));
    }

    @Test
    void testPointOnAreaEdgeIsConsideredOutside() {
        WeatherComplianceService service = new WeatherComplianceService();
        AirControlArea area = buildSquareArea();

        GeographicBoundary onEdge = new GeographicBoundary(List.of(
                new GeographicCoords(10, 5),
                new GeographicCoords(9, 5),
                new GeographicCoords(9, 6)
        ));

        assertFalse(service.isSectionValidInsideArea(area, onEdge));
    }
}
