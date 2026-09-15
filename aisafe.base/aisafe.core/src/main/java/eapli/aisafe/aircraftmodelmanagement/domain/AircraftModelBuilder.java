package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.framework.domain.model.DomainFactory;

import java.util.ArrayList;
import java.util.List;

public class AircraftModelBuilder implements DomainFactory<AircraftModel> {

    private AircraftModelId modelCode;
    private ModelName name;
    private AircraftType aircraftType;
    private Manufacturer manufacturer;
    private WeightSpecification weights;
    private WingGeometry wingGeometry;
    private AerodynamicCoefficients aerodynamics;
    private PerformanceSpec performanceSpec;
    private NumberOfEngines numberOfEngines;
    private NumberOfSeats numberOfSeats;
    private final List<EngineModel> engineModels = new ArrayList<>();

    public AircraftModelBuilder withModelCode(final AircraftModelId modelCode) {
        this.modelCode = modelCode;
        return this;
    }

    public AircraftModelBuilder withName(final ModelName name) {
        this.name = name;
        return this;
    }

    public AircraftModelBuilder withAircraftType(final AircraftType aircraftType) {
        this.aircraftType = aircraftType;
        return this;
    }

    public AircraftModelBuilder withManufacturer(final Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public AircraftModelBuilder withWeights(final WeightSpecification weights) {
        this.weights = weights;
        return this;
    }

    public AircraftModelBuilder withWingGeometry(final WingGeometry geometry) {
        this.wingGeometry = geometry;
        return this;
    }

    public AircraftModelBuilder withAerodynamics(final AerodynamicCoefficients aerodynamics) {
        this.aerodynamics = aerodynamics;
        return this;
    }

    public AircraftModelBuilder withPerformanceSpec(final PerformanceSpec performanceSpec) {
        this.performanceSpec = performanceSpec;
        return this;
    }

    public AircraftModelBuilder withNumberOfEngines(final NumberOfEngines numberOfEngines) {
        this.numberOfEngines = numberOfEngines;
        return this;
    }

    public AircraftModelBuilder withNumberOfSeats(final NumberOfSeats numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
        return this;
    }

    public AircraftModelBuilder withEngineConfiguration(final EngineModel engineModel) {
        this.engineModels.add(engineModel);
        return this;
    }

    @Override
    public AircraftModel build() {
        final AircraftModel aircraftModel = new AircraftModel(modelCode, name, aircraftType,
                manufacturer, weights, wingGeometry, aerodynamics, performanceSpec,
                numberOfEngines, numberOfSeats);

        for (EngineModel engine : engineModels) {
            aircraftModel.addEngineConfiguration(engine);
        }
        aircraftModel.ensureCertifiedEngineCount();

        return aircraftModel;
    }
}