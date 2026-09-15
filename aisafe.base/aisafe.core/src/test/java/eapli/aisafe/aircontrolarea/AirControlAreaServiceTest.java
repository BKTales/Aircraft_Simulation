package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.application.exceptions.OverlapBoundaryException;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirControlAreaServiceTest {

    @Test
    void registerNewAreaBuildsAndSavesArea() {
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(AirControlArea.class))).thenAnswer(inv -> inv.getArgument(0));

        final AirControlAreaService service = new AirControlAreaService(repository);
        final List<float[]> coords = List.of(new float[]{0, 0}, new float[]{10, 0}, new float[]{0, 10});

        final AirControlArea result = service.registerNewArea("Area-1", coords, 200f);

        assertNotNull(result);
        assertEquals("Area-1", result.getName().getName());
        verify(repository).findAll();
        verify(repository).save(any(AirControlArea.class));
    }

    @Test
    void registerNewAreaThrowsWhenBoundaryOverlaps() {
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);
        final AirControlArea existing = new AirControlArea(
                eapli.aisafe.aircontrolarea.domain.AirControlAreaName.valueOf("Existing"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(0, 0),
                        GeographicCoords.valueOf(10, 0),
                        GeographicCoords.valueOf(0, 10)
                )),
                eapli.aisafe.aircontrolarea.domain.MinFuelRequirement.valueOf(100f)
        );

        when(repository.findAll()).thenReturn(List.of(existing));

        final AirControlAreaService service = new AirControlAreaService(repository);
        final List<float[]> coords = List.of(new float[]{1, 1}, new float[]{9, 1}, new float[]{1, 9});

        assertThrows(OverlapBoundaryException.class, () -> service.registerNewArea("New", coords, 100f));
        verify(repository, never()).save(any(AirControlArea.class));
    }

    @Test
    void availableAreasDelegatesToRepository() {
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);
        final List<AirControlArea> expected = List.of();
        when(repository.findAll()).thenReturn(expected);

        final AirControlAreaService service = new AirControlAreaService(repository);

        assertSame(expected, service.availableAreas());
    }

    @Test
    void registerNewAreaChecksExistingAreasAndStillSavesWhenNoOverlap() {
        final AirControlAreaRepository repository = mock(AirControlAreaRepository.class);
        final AirControlArea existing = new AirControlArea(
                eapli.aisafe.aircontrolarea.domain.AirControlAreaName.valueOf("Existing"),
                GeographicBoundary.valueOf(List.of(
                        GeographicCoords.valueOf(32.2f, -17.9f),
                        GeographicCoords.valueOf(33.8f, -17.9f),
                        GeographicCoords.valueOf(33.8f, -16.0f),
                        GeographicCoords.valueOf(32.2f, -16.0f)
                )),
                eapli.aisafe.aircontrolarea.domain.MinFuelRequirement.valueOf(100f)
        );

        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(any(AirControlArea.class))).thenAnswer(inv -> inv.getArgument(0));

        final AirControlAreaService service = new AirControlAreaService(repository);
        final List<float[]> coords = List.of(new float[]{0, 0}, new float[]{10, 0}, new float[]{0, 10});

        final AirControlArea result = service.registerNewArea("New", coords, 100f);

        assertNotNull(result);
        verify(repository).save(any(AirControlArea.class));
    }
}
