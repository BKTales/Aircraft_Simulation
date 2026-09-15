package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EngineModelServiceTest {
    private static final String VALID_NAME = "PW4000";
    private static final String VALID_MOTOR = "turbofan";
    private static final String VALID_FUEL = "JET_A1";


    /**
     * {@link InMemoryDomainRepository} shares storage per entity class across instances; use a distinct
     * {@link ManufacturerId} in each test to avoid cross-test pollution when the full suite runs.
     */
    private static Manufacturer sampleManufacturer(final ManufacturerId id) {
        return new Manufacturer(id, new ManufacturerName("Acme"), new CountryCode("PT"));
    }

    private static class MemManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
            implements ManufacturerRepository {
        @Override
        public Optional<Manufacturer> findById(final ManufacturerId id) {
            return Optional.ofNullable(data().get(id));
        }
    }

    /**
     * Does not override {@link EngineModelRepository#existsByNameAndManufacturer} so the interface default runs.
     */
    private static class MemEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {

        @Override
        public Optional<EngineModel> findByModelId(final EngineModelId id) {
            return Optional.ofNullable(data().get(id));
        }

        @Override
        public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
            return data().values().stream()
                    .filter(e -> e.name().equals(name) && e.manufacturerId().equals(manufacturerId))
                    .findFirst();
        }
    }

    @Test
    @DisplayName("US056 - constructor rejects null engine repository")
    void constructorRejectsNullEngineModelRepository() {
        final var manufacturers = new MemManufacturerRepository();
        assertThrows(IllegalArgumentException.class,
                () -> new EngineModelService(null, manufacturers));
    }

    @Test
    @DisplayName("US056 - constructor rejects null manufacturer repository")
    void constructorRejectsNullManufacturerRepository() {
        final var engines = new MemEngineModelRepository();
        assertThrows(IllegalArgumentException.class,
                () -> new EngineModelService(engines, null));
    }

    @Test
    @DisplayName("US056 AC1 - persists valid engine model and composes id")
    void createEngineModelPersistsWhenManufacturerExistsAndNameIsUnique() {
        final ManufacturerId mid = ManufacturerId.valueOf("PER1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var engines = new MemEngineModelRepository();
        final var service = new EngineModelService(engines, manufacturers);

        final EngineModel saved = service.createEngineModel(
                VALID_NAME, "PER1", VALID_MOTOR, 100.0, 85.0, VALID_FUEL, 0.55);

        assertNotNull(saved);
        assertEquals(EngineName.valueOf(VALID_NAME), saved.name());
        assertEquals(mid, saved.manufacturerId());
        assertEquals(EngineModelId.valueOf("PER1-PW4000"), saved.identity());
        assertTrue(engines.existsByNameAndManufacturer(EngineName.valueOf(VALID_NAME), mid));
    }

    @Test
    @DisplayName("US056 AC2 - fails when manufacturer does not exist")
    void createEngineModelFailsWhenManufacturerMissing() {
        final var manufacturers = new MemManufacturerRepository();
        final var engines = new MemEngineModelRepository();
        final var service = new EngineModelService(engines, manufacturers);

        final ManufacturerNotFoundException ex = assertThrows(ManufacturerNotFoundException.class,
                () -> service.createEngineModel("X1", "ZZZNX", "turbofan", 1, 1, "JET_A1", 0.5));

        assertTrue(ex.getMessage().contains("ZZZNX"));
    }

    @Test
    @DisplayName("US056 AC3 - fails when name + manufacturer is duplicated")
    void createEngineModelFailsWhenDuplicateNameForManufacturer() {
        final ManufacturerId mid = ManufacturerId.valueOf("DUP1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var engines = new MemEngineModelRepository();
        final var service = new EngineModelService(engines, manufacturers);

        service.createEngineModel("Dup", "DUP1", "turbojet", 10, 9, "JET_B", 0.4);

        assertThrows(EngineModelAlreadyExistsException.class,
                () -> service.createEngineModel("Dup", "DUP1", "ramjet", 11, 10, "JET_A", 0.41));
    }

    @Test
    @DisplayName("US056 - maps integrity violation to already exists exception")
    void createEngineModelMapsIntegrityViolationToAlreadyExists() {
        final ManufacturerId mid = ManufacturerId.valueOf("INT1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var engines = new MemEngineModelRepository();
        final EngineModelRepository spyEngines = spy(engines);
        final var service = new EngineModelService(spyEngines, manufacturers);

        doThrow(new IntegrityViolationException("unique")).when(spyEngines).save(any(EngineModel.class));

        assertThrows(EngineModelAlreadyExistsException.class,
                () -> service.createEngineModel("NewEng", "INT1", "turboprop", 20, 18, "JET_A1", 0.42));

        verify(spyEngines).save(any(EngineModel.class));
    }

    @Test
    @DisplayName("US056 - null manufacturer id throws null pointer")
    void nullManufacturerIdThrowsNullPointerException() {
        final var service = new EngineModelService(new MemEngineModelRepository(), new MemManufacturerRepository());

        assertThrows(NullPointerException.class,
                () -> service.createEngineModel("E", null, "turbofan", 1, 1, "JET_A1", 0.5));
    }

    @Test
    @DisplayName("US056 - null fuel type throws null pointer")
    void nullFuelTypeThrowsNullPointerException() {
        final ManufacturerId mid = ManufacturerId.valueOf("FTY1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(NullPointerException.class,
                () -> service.createEngineModel("E2", "FTY1", "turbofan", 1, 1, null, 0.5));
    }

    @Test
    @DisplayName("US056 - allows same name for different manufacturers")
    void createEngineModelAllowsSameNameForDifferentManufacturers() {
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(ManufacturerId.valueOf("MFG1")));
        manufacturers.save(sampleManufacturer(ManufacturerId.valueOf("MFG2")));
        final var engines = new MemEngineModelRepository();
        final var service = new EngineModelService(engines, manufacturers);

        final EngineModel first = service.createEngineModel("CFM56", "MFG1", "turbofan", 120.0, 100.0, VALID_FUEL, 0.5);
        final EngineModel second = service.createEngineModel("CFM56", "MFG2", "turbofan", 119.0, 99.0, VALID_FUEL, 0.49);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(EngineModelId.valueOf("MFG1-CFM56"), first.identity());
        assertEquals(EngineModelId.valueOf("MFG2-CFM56"), second.identity());
    }

    @Test
    @DisplayName("US056 - allows different names for same manufacturer")
    void createEngineModelAllowsDifferentNamesForSameManufacturer() {
        final ManufacturerId mid = ManufacturerId.valueOf("MFG3");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var engines = new MemEngineModelRepository();
        final var service = new EngineModelService(engines, manufacturers);

        final EngineModel first = service.createEngineModel("A100", "MFG3", "turbojet", 90.0, 80.0, VALID_FUEL, 0.6);
        final EngineModel second = service.createEngineModel("A200", "MFG3", "turbojet", 91.0, 81.0, VALID_FUEL, 0.59);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(EngineModelId.valueOf("MFG3-A100"), first.identity());
        assertEquals(EngineModelId.valueOf("MFG3-A200"), second.identity());
    }

    @Test
    @DisplayName("US056 AC5 - rejects invalid motorization type")
    void createEngineModelRejectsInvalidMotorizationType() {
        final ManufacturerId mid = ManufacturerId.valueOf("MOT1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(IllegalArgumentException.class,
                () -> service.createEngineModel("M1", "MOT1", "rocket", 100.0, 80.0, VALID_FUEL, 0.5));
    }

    @Test
    @DisplayName("US056 - rejects invalid fuel type")
    void createEngineModelRejectsInvalidFuelType() {
        final ManufacturerId mid = ManufacturerId.valueOf("FUE1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(IllegalArgumentException.class,
                () -> service.createEngineModel("M2", "FUE1", VALID_MOTOR, 100.0, 80.0, "DIESEL_X", 0.5));
    }

    @Test
    @DisplayName("US056 AC4 - rejects non-positive thrust values")
    void createEngineModelRejectsNonPositiveThrustValues() {
        final ManufacturerId mid = ManufacturerId.valueOf("THR1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(IllegalArgumentException.class,
                () -> service.createEngineModel("M3", "THR1", VALID_MOTOR, 0.0, 80.0, VALID_FUEL, 0.5));
    }

    @Test
    @DisplayName("US056 AC4 - rejects thrust profile where static is lower than cruise")
    void createEngineModelRejectsStaticThrustLowerThanCruise() {
        final ManufacturerId mid = ManufacturerId.valueOf("THR2");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(IllegalArgumentException.class,
                () -> service.createEngineModel("M4", "THR2", VALID_MOTOR, 70.0, 80.0, VALID_FUEL, 0.5));
    }

    @Test
    @DisplayName("US056 AC6 - rejects non-positive TSFC")
    void createEngineModelRejectsNonPositiveTsfc() {
        final ManufacturerId mid = ManufacturerId.valueOf("TSF1");
        final var manufacturers = new MemManufacturerRepository();
        manufacturers.save(sampleManufacturer(mid));
        final var service = new EngineModelService(new MemEngineModelRepository(), manufacturers);

        assertThrows(IllegalArgumentException.class,
                () -> service.createEngineModel("M5", "TSF1", VALID_MOTOR, 100.0, 80.0, VALID_FUEL, 0.0));
    }
}
