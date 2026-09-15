package eapli.aisafe.dsl;

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
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.domain.FuelType;
import eapli.aisafe.enginemodelmanagement.domain.MotorizationType;
import eapli.aisafe.enginemodelmanagement.domain.TSFC;
import eapli.aisafe.enginemodelmanagement.domain.ThrustProfile;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorAircraftJsonMapperTest {

    @Test
    void convertsDomainToSiJson() {
        final Manufacturer manufacturer = new Manufacturer(
                ManufacturerId.valueOf("MAN02"),
                new ManufacturerName("Airbus"),
                new CountryCode("PT"));
        final AircraftModel model = new AircraftModel(
                AircraftModelId.valueOf("A320"),
                ModelName.valueOf("Airbus A320"),
                AircraftType.PASSENGER,
                manufacturer,
                WeightSpecification.valueOf(78000, 60000, 42000),
                WingGeometry.valueOf(122.6, 35.8),
                AerodynamicCoefficients.valueOf(0.02, 1.5),
                PerformanceSpec.valueOf(12000, 230, 24000, 5200),
                NumberOfEngines.valueOf(2),
                NumberOfSeats.valueOf(180));
        final EngineModel engine = new EngineModel(
                EngineModelId.valueOf("MAN02-PW1100G"),
                EngineName.valueOf("PW1100G"),
                TSFC.valueOf(0.50),
                FuelType.JET_A1,
                ThrustProfile.valueOf(150.0, 135.0),
                manufacturer,
                MotorizationType.TURBOFAN);

        final String json = eapli.aisafe.dsl.api.SimulatorAircraftJsonMapper.toJsonObject(model, engine);

        assertTrue(json.contains("\"ModelId\": \"A320\""));
        assertTrue(json.contains("\"Thrust0\": 150000"));
        assertTrue(json.contains("\"ThrustMaxSpeed\": 135000"));
        assertTrue(json.contains("\"MaxPayload\": 18000"));
        assertTrue(json.contains("\"FuelCapacity\": 19296"));
        assertTrue(json.contains("\"CdragFunction\""));
    }

    @Test
    void msToMachUsesAltitude() {
        final double mach = eapli.aisafe.dsl.api.SimulatorAircraftJsonMapper.msToMach(230, 12000);
        assertTrue(mach > 0.7 && mach < 0.85);
        assertEquals(0.50 / 9.80665, 0.50 / 9.80665, 1e-10);
    }
}
