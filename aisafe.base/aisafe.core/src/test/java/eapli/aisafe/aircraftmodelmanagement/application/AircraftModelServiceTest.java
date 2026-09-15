package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.*;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.enginemodelmanagement.application.ManufacturerNotFoundException;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftModelServiceTest {

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private EngineModelRepository engineModelRepository;

    @InjectMocks
    private AircraftModelService service;

    @Test
    void shouldCreateAircraftModelSuccessfully() {
        // Arrange
        String mId = "A320";
        String mName = "Airbus A320";
        String manuIdStr = "AIRBUS";
        List<String> engineIds = List.of("CFM56");

        Manufacturer manufacturer = mock(Manufacturer.class);
        EngineModel engine = mock(EngineModel.class);
        EngineModelId eId = EngineModelId.valueOf("CFM56");

        when(manufacturer.identity()).thenReturn(ManufacturerId.valueOf(manuIdStr));
        when(manufacturerRepository.findById(any(ManufacturerId.class))).thenReturn(Optional.of(manufacturer));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());
        when(engineModelRepository.findByModelId(eId)).thenReturn(Optional.of(engine));
        when(engine.identity()).thenReturn(eId);
        when(aircraftModelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AircraftModel result = service.createAircraftModel(
                mId, mName, AircraftType.PASSENGER, manuIdStr,
                78000, 60000, 42000, 122.6, 35.8,
                0.02, 1.5, 12000, 830, 24000, 6000, 180, 2, engineIds
        );

        // Assert
        assertEquals(AircraftModelId.valueOf("A320"), result.identity());
        assertEquals(ModelName.valueOf("Airbus A320"), result.name());
        assertEquals(AircraftType.PASSENGER, result.aircraftType());
        assertEquals(ManufacturerId.valueOf("AIRBUS"), result.manufacturer().identity());
        assertEquals(1, result.engineCertifiedConfigurations().size());
        assertTrue(result.engineCertifiedConfigurations().stream()
                .anyMatch(cfg -> cfg.engineModel().identity().equals(EngineModelId.valueOf("CFM56"))));
        verify(aircraftModelRepository).save(any(AircraftModel.class));
        verify(engineModelRepository).findByModelId(eId);
    }

    @Test
    void shouldThrowExceptionWhenManufacturerNotFound() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class, () ->
                service.createAircraftModel("A1", "N1", AircraftType.CARGO, "UNKNOWN",
                        1, 1, 1, 1, 1, 0.1, 0.1, 1, 1, 1, 1, 1, 1, List.of("E1"))
        );
    }

    @Test
    void shouldThrowExceptionWhenModelAlreadyExistsForManufacturer() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(mock(Manufacturer.class)));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.of(mock(AircraftModel.class)));

        assertThrows(AircraftModelAlreadyExistsException.class, () ->
                service.createAircraftModel("A1", "ExistingName", AircraftType.PASSENGER, "MANU",
                        1, 1, 1, 1, 1, 0.1, 0.1, 1, 1, 1, 1, 1, 1, List.of("E1"))
        );
    }

    @Test
    void shouldThrowExceptionWhenEngineModelNotFound() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(mock(Manufacturer.class)));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());
        when(engineModelRepository.findByModelId(any())).thenReturn(Optional.empty());

        assertThrows(EngineModelNotFoundException.class, () ->
                service.createAircraftModel("A1", "N1", AircraftType.PASSENGER, "MANU",
                        1, 1, 1, 1, 1, 0.1, 0.1, 1, 1, 1, 1, 1, 1, List.of("INVALID_ENG"))
        );
    }

    @Test
    void shouldThrowExceptionWhenEngineListIsEmpty() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(mock(Manufacturer.class)));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createAircraftModel("A1", "N1", AircraftType.PASSENGER, "MANU",
                        1, 1, 1, 1, 1, 0.1, 0.1, 1, 1, 1, 1, 1, 1, List.of())
        );
    }

    @Test
    void shouldThrowExceptionWhenEngineListIsNull() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(mock(Manufacturer.class)));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createAircraftModel("A1", "N1", AircraftType.PASSENGER, "MANU",
                        1, 1, 1, 1, 1, 0.1, 0.1, 1, 1, 1, 1, 1, 1, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenSeatCountIsNonPositive() {
        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(mock(Manufacturer.class)));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createAircraftModel("A1", "N1", AircraftType.PASSENGER, "MANU",
                        10, 9, 8, 7, 6, 0.1, 0.1, 5, 4, 3, 2, 0, 1, List.of("E1"))
        );
    }

    @Test
    void shouldHandleIntegrityViolationExceptionOnSave() {

        String manuIdStr = "MANU01";
        String engineIdStr = "ENG01";
        EngineModelId engineId = EngineModelId.valueOf(engineIdStr);


        Manufacturer manufacturer = mock(Manufacturer.class);
        EngineModel engine = mock(EngineModel.class);


        when(manufacturerRepository.findById(any())).thenReturn(Optional.of(manufacturer));
        when(aircraftModelRepository.existsByNameAndManufacturer(any(), any())).thenReturn(Optional.empty());


        when(engineModelRepository.findByModelId(engineId)).thenReturn(Optional.of(engine));


        when(aircraftModelRepository.save(any())).thenThrow(new IntegrityViolationException());


        assertThrows(AircraftModelAlreadyExistsException.class, () ->
                service.createAircraftModel(
                        "A320", "Airbus A320", AircraftType.PASSENGER, manuIdStr,
                        78000, 60000, 42000,
                        122.6, 35.8, 0.02, 1.5, 12000, 830, 24000, 6000,
                        180, 1, List.of(engineIdStr)
                )
        );
    }

    @Test
    void ensureConstructorThrowsOnNullRepositories() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelService(null, manufacturerRepository, engineModelRepository));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelService(aircraftModelRepository, null, engineModelRepository));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelService(aircraftModelRepository, manufacturerRepository, null));
    }

    @Test
    void addEngineModelToAircraftModelShouldSaveUpdatedModel() {
        final AircraftModel model = mock(AircraftModel.class);
        final EngineModel engine = mock(EngineModel.class);
        final EngineModelId engineId = EngineModelId.valueOf("ENG-1");

        when(aircraftModelRepository.findByID(any(AircraftModelId.class))).thenReturn(Optional.of(model));
        when(engineModelRepository.findByModelId(engineId)).thenReturn(Optional.of(engine));
        when(aircraftModelRepository.save(model)).thenReturn(model);

        final AircraftModel result = service.addEngineModelToAircraftModel("A320", "ENG-1");

        assertSame(model, result);
        verify(model).addEngineConfiguration(engine);
        verify(aircraftModelRepository).save(model);
    }

    @Test
    void addEngineModelToAircraftModelShouldThrowWhenAircraftMissing() {
        when(aircraftModelRepository.findByID(any(AircraftModelId.class))).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class,
                () -> service.addEngineModelToAircraftModel("A320", "ENG-1"));
        verify(aircraftModelRepository, never()).save(any());
    }

    @Test
    void addEngineModelToAircraftModelShouldThrowWhenEngineMissing() {
        final AircraftModel model = mock(AircraftModel.class);
        when(aircraftModelRepository.findByID(any(AircraftModelId.class))).thenReturn(Optional.of(model));
        when(engineModelRepository.findByModelId(any(EngineModelId.class))).thenReturn(Optional.empty());

        assertThrows(EngineModelNotFoundException.class,
                () -> service.addEngineModelToAircraftModel("A320", "ENG-1"));
        verify(aircraftModelRepository, never()).save(any());
    }
}
