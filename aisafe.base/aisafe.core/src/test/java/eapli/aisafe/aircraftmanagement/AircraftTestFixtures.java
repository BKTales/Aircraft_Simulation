package eapli.aisafe.aircraftmanagement;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;
import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;
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
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
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

import java.time.Year;

public final class AircraftTestFixtures {

    private static final Manufacturer STUB_MANUFACTURER = new Manufacturer(
            ManufacturerId.valueOf("STUB"),
            new ManufacturerName("Stub"),
            new CountryCode("PT"));

    private AircraftTestFixtures() {}

    public static AircraftModel stubModel(final String modelCode) {
        return new AircraftModel(
                AircraftModelId.valueOf(modelCode),
                ModelName.valueOf("Stub-" + modelCode),
                AircraftType.PASSENGER,
                STUB_MANUFACTURER,
                WeightSpecification.valueOf(100, 80, 50),
                WingGeometry.valueOf(120, 35),
                AerodynamicCoefficients.valueOf(0.02, 1.5),
                PerformanceSpec.valueOf(12000, 230, 24000, 5000),
                NumberOfEngines.valueOf(2),
                NumberOfSeats.valueOf(180));
    }

    public static EngineModel stubEngine(final String engineCode) {
        return new EngineModel(
                EngineModelId.valueOf(engineCode),
                EngineName.valueOf("StubEngine"),
                TSFC.valueOf(0.5),
                FuelType.JET_A1,
                ThrustProfile.valueOf(100, 80),
                STUB_MANUFACTURER,
                MotorizationType.TURBOFAN);
    }

    public static Aircraft sampleAircraft(final String registration,
                                          final String modelCode,
                                          final String engineCode,
                                          final IATACode owner,
                                          final OperationalStatus status,
                                          final int seats,
                                          final int ageInYears) {
        return new Aircraft(
                new AircraftRegistration(registration),
                stubModel(modelCode),
                stubEngine(engineCode),
                CabinConfiguration.ofEconomyBusinessFirst(seats, 0, 0),
                new RegistrationCountry("PT"),
                owner,
                status,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - ageInYears));
    }
}
