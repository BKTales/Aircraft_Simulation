package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AircraftModelTest {

    private AircraftModelId id;
    private ModelName name;
    private AircraftType type;
    private Manufacturer manufacturer;
    private WeightSpecification weights;
    private WingGeometry geometry;
    private AerodynamicCoefficients aerodynamics;
    private PerformanceSpec performance;
    private NumberOfEngines numEngines;
    private NumberOfSeats capacitySeats;

    @BeforeEach
    void setUp() {
        id = mock(AircraftModelId.class);
        name = mock(ModelName.class);
        type = AircraftType.PASSENGER;
        manufacturer = mock(Manufacturer.class);
        weights = mock(WeightSpecification.class);
        geometry = mock(WingGeometry.class);
        aerodynamics = mock(AerodynamicCoefficients.class);
        performance = mock(PerformanceSpec.class);
        numEngines = mock(NumberOfEngines.class);
        capacitySeats = mock(NumberOfSeats.class);
    }

    @Test
    void ensureAircraftModelIsCreatedCorrectly() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);

        assertNotNull(subject);
        assertEquals(id, subject.identity());
        assertEquals(name, subject.name());
        assertEquals(type, subject.aircraftType());
        assertEquals(manufacturer, subject.manufacturer());
        assertEquals(weights, subject.weights());
        assertEquals(geometry, subject.wingGeometry());
        assertEquals(aerodynamics, subject.aerodynamics());
        assertEquals(performance, subject.performanceSpec());
        assertEquals(numEngines, subject.numberOfEngines());
        assertEquals(capacitySeats, subject.numberOfSeats());
    }

    @Test
    void ensureConstructorThrowsExceptionOnNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(null, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, null, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, null, geometry, aerodynamics, performance, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, weights, null, aerodynamics, performance, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, weights, geometry, null, performance, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, null, numEngines, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, null, capacitySeats));
        assertThrows(IllegalArgumentException.class, () -> new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, null));
    }

    @Test
    void ensureAddEngineConfigurationWorks() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);

        EngineModelId engineId = mock(EngineModelId.class);
        EngineModel engineModel = mock(EngineModel.class);
        when(engineModel.identity()).thenReturn(engineId);

        subject.addEngineConfiguration(engineModel);

        List<EngineConfiguration> configs = subject.engineCertifiedConfigurations();
        assertEquals(1, configs.size());
        assertEquals(engineModel, configs.get(0).engineModel());
    }

    @Test
    void ensureAddEngineConfigurationThrowsOnNull() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);
        assertThrows(IllegalArgumentException.class, () -> subject.addEngineConfiguration(null));
    }

    @Test
    void ensureDuplicateEngineCertificationIsPrevented() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);

        EngineModelId engineId = mock(EngineModelId.class);
        EngineModel engineModel = mock(EngineModel.class);
        when(engineModel.identity()).thenReturn(engineId);

        subject.addEngineConfiguration(engineModel);

        assertThrows(IllegalArgumentException.class, () -> subject.addEngineConfiguration(engineModel));
    }

    @Test
    void ensureAddEngineConfigurationIgnoresNullEntriesAlreadyInList() throws Exception {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);
        java.lang.reflect.Field field = AircraftModel.class.getDeclaredField("certifiedConfigurations");
        field.setAccessible(true);
        List<EngineConfiguration> list = new ArrayList<>();
        list.add(null);
        field.set(subject, list);

        EngineModelId engineId = mock(EngineModelId.class);
        EngineModel engineModel = mock(EngineModel.class);
        when(engineModel.identity()).thenReturn(engineId);

        assertDoesNotThrow(() -> subject.addEngineConfiguration(engineModel));
        assertEquals(2, subject.engineCertifiedConfigurations().size());
    }

    @Test
    void ensureCertifiedEngineCountLogic() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);

        assertThrows(IllegalArgumentException.class, subject::ensureCertifiedEngineCount);

        EngineModel engineModel = mock(EngineModel.class);
        when(engineModel.identity()).thenReturn(mock(EngineModelId.class));

        subject.addEngineConfiguration(engineModel);
        assertDoesNotThrow(subject::ensureCertifiedEngineCount);
    }

    @Test
    void testSameAsAndEquals() {
        AircraftModel subject = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);
        AircraftModel same = new AircraftModel(id, name, type, manufacturer, weights, geometry, aerodynamics, performance, numEngines, capacitySeats);

        assertTrue(subject.sameAs(same));
        assertTrue(subject.sameAs(subject));

        assertFalse(subject.sameAs(null));
        assertFalse(subject.sameAs(new Object()));
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        AircraftModel subject = new AircraftModel() {};
        assertNotNull(subject);
    }
}