package eapli.aisafe.flightmanagement;

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
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.domain.FuelType;
import eapli.aisafe.enginemodelmanagement.domain.MotorizationType;
import eapli.aisafe.enginemodelmanagement.domain.TSFC;
import eapli.aisafe.enginemodelmanagement.domain.ThrustProfile;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public final class SimulatorJsonTestFixtures {

    public static final AircraftModelId MODEL_ID = AircraftModelId.valueOf("A320");
    public static final EngineModelId ENGINE_ID = EngineModelId.valueOf("MAN02-PW1100G");

    private SimulatorJsonTestFixtures() {}

    public static AircraftModel sampleA320Model() {
        final Manufacturer manufacturer = new Manufacturer(
                ManufacturerId.valueOf("MAN02"),
                new ManufacturerName("Airbus"),
                new CountryCode("PT"));
        return new AircraftModel(
                MODEL_ID,
                ModelName.valueOf("Airbus A320"),
                AircraftType.PASSENGER,
                manufacturer,
                WeightSpecification.valueOf(78000, 60000, 42000),
                WingGeometry.valueOf(122.6, 35.8),
                AerodynamicCoefficients.valueOf(0.02, 1.5),
                PerformanceSpec.valueOf(12000, 230, 24000, 5200),
                NumberOfEngines.valueOf(2),
                NumberOfSeats.valueOf(180));
    }

    public static EngineModel sampleEngine() {
        return new EngineModel(
                ENGINE_ID,
                EngineName.valueOf("PW1100G"),
                TSFC.valueOf(0.50),
                FuelType.JET_A1,
                ThrustProfile.valueOf(150.0, 135.0),
                new Manufacturer(
                        ManufacturerId.valueOf("MAN02"),
                        new ManufacturerName("Airbus"),
                        new CountryCode("PT")),
                MotorizationType.TURBOFAN);
    }

    public static void seedA320(final AircraftModelRepository models, final EngineModelRepository engines) {
        models.save(sampleA320Model());
        engines.save(sampleEngine());
    }

    public static InMemoryEngineModelRepository newEngineRepository() {
        return new InMemoryEngineModelRepository();
    }

    public static String minimalSelfContainedJson(final String areaCode) {
        return """
                {
                  "ID": 1,
                  "Type": "regular",
                  "Route": "TP100",
                  "AircraftId": "A320",
                  "Aircraft": { "ModelId": "A320", "FuelCapacity": 19296, "MTOW": 78000, "EWeight": 42000 },
                  "Legs": [{
                    "Departure": "OPO",
                    "DepartureAirport": { "Id": "OPO", "AreaCode": "%s", "Latitude": 41.24, "Longitude": -8.68, "Altitude": 69 },
                    "Arrival": "LIS",
                    "ArrivalAirport": { "Id": "LIS", "AreaCode": "%s", "Latitude": 38.77, "Longitude": -9.13, "Altitude": 113 },
                    "Fuel": { "Quantity": 12000, "Unit": "kg" },
                    "Load": { "Passengers": 100, "PassengerWeight": { "Quantity": 8000, "Unit": "kg" }, "CargoWeight": { "Quantity": 500, "Unit": "kg" } },
                    "Segments": [{ "Mode": "climb", "Start": { "Latitude": 41.24, "Longitude": -8.68, "Altitude": { "Quantity": 69, "Unit": "m" } }, "End": { "Latitude": 38.77, "Longitude": -9.13, "Altitude": { "Quantity": 11000, "Unit": "m" } } }]
                  }]
                }
                """.formatted(areaCode, areaCode);
    }

    public static final class InMemoryEngineModelRepository
            extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {

        @Override
        public java.util.Optional<EngineModel> findByModelId(final EngineModelId id) {
            return ofIdentity(id);
        }

        @Override
        public java.util.Optional<EngineModel> findByNameAndManufacturer(
                final EngineName name, final ManufacturerId manufacturerId) {
            return java.util.Optional.empty();
        }
    }
}
