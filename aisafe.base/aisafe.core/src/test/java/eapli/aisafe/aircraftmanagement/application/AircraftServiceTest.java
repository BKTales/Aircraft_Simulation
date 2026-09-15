package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.domain.AerodynamicCoefficients;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfEngines;
import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfSeats;
import eapli.aisafe.aircraftmodelmanagement.domain.PerformanceSpec;
import eapli.aisafe.aircraftmodelmanagement.domain.WeightSpecification;
import eapli.aisafe.aircraftmodelmanagement.domain.WingGeometry;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private AircraftService service;

    @Test
    void constructorRejectsNullRepositories() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftService(null, aircraftModelRepository));
        assertThrows(IllegalArgumentException.class, () -> new AircraftService(aircraftRepository, null));
    }

    @Test
    void registerThrowsWhenOwnerCompanyMissing() {
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        assertThrows(NullPointerException.class,
                () -> service.registerAircraft("CS-AAA", "M1", "E1", cabin, "PT", 2, 2020, null));
    }

    @Test
    void registerThrowsWhenDuplicateRegistration() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(true);
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);

        assertThrows(DuplicateAircraftRegistrationException.class,
                () -> service.registerAircraft("CS-DUP", "M1", "E1", cabin, "PT", 2, 2020, IATACode.valueOf("TP")));

        verify(aircraftModelRepository, never()).findByID(any());
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenModelNotFound() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(false);
        when(aircraftModelRepository.findByID(any())).thenReturn(Optional.empty());
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);

        assertThrows(AircraftModelNotFoundException.class,
                () -> service.registerAircraft("CS-NOM", "UNKNOWN", "E1", cabin, "PT", 2, 2020, IATACode.valueOf("TP")));

        verify(aircraftRepository, never()).save(any());
    }


    private static AircraftModel modelWithCapacityAndEngine(final int maxSeats, final String engineId) {
        final AircraftModel m = new AircraftModel(
                AircraftModelId.valueOf("M1"),
                ModelName.valueOf("Test Model"),
                AircraftType.PASSENGER,
                new Manufacturer(ManufacturerId.valueOf("MAN01"), new ManufacturerName("John"), new CountryCode("PT")),
                WeightSpecification.valueOf(100, 80, 50),
                WingGeometry.valueOf(120, 35),
                AerodynamicCoefficients.valueOf(0.02, 1.5),
                PerformanceSpec.valueOf(12000, 230, 24000, 5000),
                NumberOfEngines.valueOf(2),
                NumberOfSeats.valueOf(maxSeats));

        EngineModelId realEngineId = EngineModelId.valueOf(engineId);


        EngineModel mockEngineModel = mock(EngineModel.class);
        when(mockEngineModel.identity()).thenReturn(realEngineId);

        m.addEngineConfiguration(mockEngineModel);
        return m;
    }

    @Test
    void registerRejectsWhenTotalSeatsExceedModelCapacity() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(false);
        final AircraftModel model = modelWithCapacityAndEngine(10, "E1");
        when(aircraftModelRepository.findByID(any())).thenReturn(Optional.of(model));
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(8, 2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.registerAircraft("CS-XYZ", "M1", "E1", cabin, "PT", 2, 1, IATACode.valueOf("TP")));

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void registerAcceptsCabinWithinModelCapacity() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(false);
        final AircraftModel model = modelWithCapacityAndEngine(20, "E1");
        when(aircraftModelRepository.findByID(any())).thenReturn(Optional.of(model));
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(inv -> inv.getArgument(0));
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(10, 5, 0);

        assertDoesNotThrow(() -> service.registerAircraft("CS-ABC", "M1", "E1", cabin, "PT", 2, 2020, IATACode.valueOf("TP")));

        verify(aircraftRepository).save(any(Aircraft.class));
    }

    @Test
    void registerRejectsWhenEngineNotCertifiedForModel() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(false);
        final AircraftModel model = modelWithCapacityAndEngine(100, "E1");
        when(aircraftModelRepository.findByID(any())).thenReturn(Optional.of(model));
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(50, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> service.registerAircraft("CS-ENG", "M1", "OTHER", cabin, "PT", 2, 1, IATACode.valueOf("TP")));

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void registerPersistsActiveAircraftWhenRulesSatisfied() {
        when(aircraftRepository.existsByRegistration(any())).thenReturn(false);
        final AircraftModel model = modelWithCapacityAndEngine(50, "E1");
        when(aircraftModelRepository.findByID(any())).thenReturn(Optional.of(model));
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(inv -> inv.getArgument(0));
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(40, 10, 0);

        final Aircraft saved = service.registerAircraft(
                "CS-TEST", "M1", "E1", cabin, "PT", 2, 2019, IATACode.valueOf("TP"));

        assertNotNull(saved);
        verify(aircraftRepository).save(any(Aircraft.class));
    }
}
