package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
import eapli.aisafe.aircontrolarea.repositories.InMemoryAirControlAreaRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    private static final double LAT = 38.5;
    private static final double LON = -8.5;
    private static final String VALID_IATA = "ZZZ";
    private static final String VALID_ICAO = "ZZZ1";

    @Mock
    private AirportRepository mockAirportRepository;

    @Mock
    private AirControlAreaRepository mockAreaRepository;

    private InMemoryAirControlAreaRepository inMemoryAreaRepository;
    private InMemoryAirportRepository inMemoryAirportRepository;

    @BeforeEach
    void setUpInMemoryRepos() {
        inMemoryAreaRepository = new InMemoryAirControlAreaRepository();
        inMemoryAirportRepository = new InMemoryAirportRepository();

        final AirControlArea area = new AirControlArea(
                new AirControlAreaName("Test"),
                new GeographicBoundary(List.of(
                        new GeographicCoords(38.0f, -9.0f),
                        new GeographicCoords(39.0f, -9.0f),
                        new GeographicCoords(39.0f, -8.0f),
                        new GeographicCoords(38.0f, -8.0f))),
                new MinFuelRequirement(100.0f));
        inMemoryAreaRepository.save(area);
    }

    @Test
    @DisplayName("US052 - constructor rejects null airport repository")
    void constructorRejectsNullAirportRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new AirportService(null, inMemoryAreaRepository));
    }

    @Test
    @DisplayName("US052 - constructor rejects null area repository")
    void constructorRejectsNullAreaRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new AirportService(inMemoryAirportRepository, null));
    }

    @Test
    @DisplayName("US052 AC1/AC5 - valid registration persists airport and normalizes codes")
    void registerAirportPersistsWhenValid() {
        final GeographicBoundary hit = mock(GeographicBoundary.class);
        when(hit.contains(any(GeographicCoords.class))).thenReturn(true);
        final AirControlArea area = mock(AirControlArea.class);
        when(area.getGeographicBoundary()).thenReturn(hit);
        when(area.getAreaCode()).thenReturn(new AreaCode("AC-TEST"));
        when(mockAreaRepository.findAll()).thenReturn(List.of(area));

        final AirportService service = new AirportService(inMemoryAirportRepository, mockAreaRepository);
        final Airport saved = service.registerAirport(" zzz ", " zzz1 ", LAT, LON, 50.0);
        assertNotNull(saved);
        assertEquals(AirportIATACode.valueOf(VALID_IATA), saved.identity());
        assertEquals(AirportICAOCode.valueOf(VALID_ICAO), saved.icaoCode());
        assertTrue(saved.airControlAreaCode() != null && !saved.airControlAreaCode().isBlank());
    }

    @Test
    @DisplayName("US052 - service picks first area that contains coordinates")
    void registerAirportUsesSecondAreaWhenFirstDoesNotContainPoint() {
        final GeographicBoundary miss = mock(GeographicBoundary.class);
        when(miss.contains(any(GeographicCoords.class))).thenReturn(false);

        final GeographicBoundary hit = mock(GeographicBoundary.class);
        when(hit.contains(any(GeographicCoords.class))).thenReturn(true);

        final AirControlArea areaSkip = mock(AirControlArea.class);
        when(areaSkip.getGeographicBoundary()).thenReturn(miss);

        final AreaCode expectedCode = new AreaCode("HIT-AREA");
        final AirControlArea areaContain = mock(AirControlArea.class);
        when(areaContain.getGeographicBoundary()).thenReturn(hit);
        when(areaContain.getAreaCode()).thenReturn(expectedCode);

        when(mockAreaRepository.findAll()).thenReturn(List.of(areaSkip, areaContain));

        final AirportService service = new AirportService(inMemoryAirportRepository, mockAreaRepository);
        final Airport saved = service.registerAirport("AAA", "AAA1", LAT, LON, 1.0);
        assertEquals("HIT-AREA", saved.airControlAreaCode());
    }

    @Test
    @DisplayName("US052 AC2 - fails when coordinates do not belong to any area")
    void registerAirportFailsWhenNoAreaContainsCoordinates() {
        when(mockAreaRepository.findAll()).thenReturn(List.of());
        final AirportService service = new AirportService(inMemoryAirportRepository, mockAreaRepository);
        assertThrows(NoAreaFoundForCoordinatesException.class,
                () -> service.registerAirport("AAA", "AAA1", LAT, LON, 10));
    }

    @Test
    @DisplayName("US052 AC3 - fails when IATA already exists before save")
    void registerAirportFailsWhenIataAlreadyExistsBeforeSave() {
        final AirportService service = new AirportService(inMemoryAirportRepository, inMemoryAreaRepository);
        final String iata = "QZX";
        final String icao = "QZX1";
        service.registerAirport(iata, icao, LAT, LON, 100.0);
        assertThrows(AirportIATACodeAlreadyExistsException.class,
                () -> service.registerAirport(iata, "NEW1", LAT, LON, 100.0));
    }

    @Test
    @DisplayName("US052 AC4 - fails when ICAO already exists before save")
    void registerAirportFailsWhenIcaoAlreadyExistsBeforeSave() {
        final AirportService service = new AirportService(inMemoryAirportRepository, inMemoryAreaRepository);
        final String iata1 = "WVU";
        final String iata2 = "KLM";
        final String icao = "WV01";
        service.registerAirport(iata1, icao, LAT, LON, 100.0);
        assertThrows(AirportICAOCodeAlreadyExistsException.class,
                () -> service.registerAirport(iata2, icao, LAT, LON, 100.0));
    }

    @Test
    @DisplayName("US052 AC6 - fails when latitude is outside global limits")
    void registerAirportFailsWhenLatitudeOutsideGlobalBounds() {
        final AirportService service = new AirportService(inMemoryAirportRepository, inMemoryAreaRepository);
        assertThrows(IllegalArgumentException.class,
                () -> service.registerAirport("AAA", "AAA1", 91.0, LON, 100.0));
    }

    @Test
    @DisplayName("US052 - propagates unknown integrity violation")
    void registerAirportPropagatesIntegrityViolationWhenCauseUnknown() {
        when(mockAreaRepository.findAll()).thenAnswer(inv -> inMemoryAreaRepository.findAll());
        when(mockAirportRepository.ofIdentity(any(AirportIATACode.class))).thenReturn(Optional.empty());
        when(mockAirportRepository.findByIcaoCode(any(AirportICAOCode.class))).thenReturn(Optional.empty());
        when(mockAirportRepository.save(any(Airport.class)))
                .thenThrow(new IntegrityViolationException("constraint"));

        final AirportService service = new AirportService(mockAirportRepository, mockAreaRepository);

        assertThrows(IntegrityViolationException.class,
                () -> service.registerAirport("BBB", "BBB1", LAT, LON, 1.0));
    }

    @Test
    @DisplayName("US052 - maps integrity violation to duplicate IATA exception")
    void registerAirportMapsIntegrityViolationToIataWhenIdentityPresent() {
        when(mockAreaRepository.findAll()).thenAnswer(inv -> inMemoryAreaRepository.findAll());
        final Airport existing = new Airport(
                AirportIATACode.valueOf("CCC"),
                AirportICAOCode.valueOf("CCC1"),
                Coordinates.valueOf(LAT, LON, 1.0),
                new AreaCode());
        when(mockAirportRepository.ofIdentity(any(AirportIATACode.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(mockAirportRepository.findByIcaoCode(any(AirportICAOCode.class))).thenReturn(Optional.empty());
        when(mockAirportRepository.save(any(Airport.class)))
                .thenThrow(new IntegrityViolationException("dup"));

        final AirportService service = new AirportService(mockAirportRepository, mockAreaRepository);

        assertThrows(AirportIATACodeAlreadyExistsException.class,
                () -> service.registerAirport("CCC", "CCD1", LAT, LON, 1.0));
    }

    @Test
    @DisplayName("US052 - maps integrity violation to duplicate ICAO exception")
    void registerAirportMapsIntegrityViolationToIcaoWhenIcaoPresent() {
        when(mockAreaRepository.findAll()).thenAnswer(inv -> inMemoryAreaRepository.findAll());
        final Airport existing = new Airport(
                AirportIATACode.valueOf("DDD"),
                AirportICAOCode.valueOf("DDD1"),
                Coordinates.valueOf(LAT, LON, 1.0),
                new AreaCode());
        when(mockAirportRepository.ofIdentity(any(AirportIATACode.class))).thenReturn(Optional.empty());
        when(mockAirportRepository.findByIcaoCode(any(AirportICAOCode.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(mockAirportRepository.save(any(Airport.class)))
                .thenThrow(new IntegrityViolationException("dup"));

        final AirportService service = new AirportService(mockAirportRepository, mockAreaRepository);

        assertThrows(AirportICAOCodeAlreadyExistsException.class,
                () -> service.registerAirport("DDE", "DDD1", LAT, LON, 1.0));
    }
}
