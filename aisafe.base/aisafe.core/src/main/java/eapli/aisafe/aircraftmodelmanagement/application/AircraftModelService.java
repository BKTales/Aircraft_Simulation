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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AircraftModelService {

    private final AircraftModelRepository aircraftModels;
    private final ManufacturerRepository manufacturers;
    private final EngineModelRepository engineModels;

    public AircraftModelService(final AircraftModelRepository aircraftModels,
                                final ManufacturerRepository manufacturers,
                                final EngineModelRepository engineModels) {
        if (aircraftModels == null || manufacturers == null || engineModels == null) {
            throw new IllegalArgumentException("Aircraft model, manufacturer and engine model repositories are required.");
        }
        this.aircraftModels = aircraftModels;
        this.manufacturers = manufacturers;
        this.engineModels = engineModels;
    }

    public AircraftModel createAircraftModel(final String modelId,
                                             final String modelName,
                                             final AircraftType aircraftType,
                                             final String manufacturerId,
                                             final double mtow,
                                             final double mzfw,
                                             final double emptyWeight,
                                             final double wingArea,
                                             final double wingSpan,
                                             final double cd0,
                                             final double cl,
                                             final double serviceCeiling,
                                             final double cruiseSpeed,
                                             final double fuelCapacity,
                                             final double maxRange,
                                             final int maxPassengerSeats,
                                             final int numberOfEngines,
                                             final List<String> engineModelIds) {

        final ManufacturerId manuId =  ManufacturerId.valueOf(manufacturerId.trim());
        if (manufacturers.findById(manuId).isEmpty()) {
            throw new ManufacturerNotFoundException("Manufacturer not found: " + manuId);
        }

        final Manufacturer manufacturer = manufacturers.findById(manuId).get();

        final ModelName name = ModelName.valueOf(modelName);


        if (aircraftModels.existsByNameAndManufacturer(name,manuId).isPresent()) {
            throw new AircraftModelAlreadyExistsException("Aircraft model already registered for this name and manufacturer combination.");
        }

        if (engineModelIds == null || engineModelIds.isEmpty()) {
            throw new IllegalArgumentException("At least one engine model is required.");
        }

        if (maxPassengerSeats < 1) {
            throw new IllegalArgumentException("Maximum passenger seats for the aircraft model must be positive.");
        }

        final List<EngineModel> engines = resolveEngines(engineModelIds);

        final AircraftModelBuilder builder = new AircraftModelBuilder();

        builder.withModelCode(AircraftModelId.valueOf(modelId))
                .withName(name)
                .withAircraftType(aircraftType)
                .withManufacturer(manufacturer)
                .withWeights(WeightSpecification.valueOf(mtow, mzfw, emptyWeight))
                .withWingGeometry(WingGeometry.valueOf(wingArea, wingSpan))
                .withAerodynamics(AerodynamicCoefficients.valueOf(cd0, cl))
                .withPerformanceSpec(PerformanceSpec.valueOf(serviceCeiling, cruiseSpeed, fuelCapacity, maxRange))
                .withNumberOfEngines(NumberOfEngines.valueOf(numberOfEngines))
                .withNumberOfSeats(NumberOfSeats.valueOf(maxPassengerSeats));

        for (final EngineModel engine : engines) {
            builder.withEngineConfiguration(engine);
        }

        final AircraftModel model = builder.build();
        try {
            return aircraftModels.save(model);
        } catch (final IntegrityViolationException ex) {
            throw new AircraftModelAlreadyExistsException("Aircraft model already exists.");
        }
    }

    private List<EngineModel> resolveEngines(final List<String> engineModelIds) {
        return engineModelIds.stream()
                .map(this::requireEngine)
                .toList();
    }

    private EngineModel requireEngine(final String engineModelId) {
        final EngineModelId id = EngineModelId.valueOf(Objects.requireNonNull(engineModelId, "engineModelId").trim());
        return engineModels.findByModelId(id)
                .orElseThrow(() -> new EngineModelNotFoundException("Engine model not found: " + id));
    }

    public AircraftModel addEngineModelToAircraftModel(final String aircraftModelId,
                                                       final String engineModelId) {
        final AircraftModelId aid = AircraftModelId.valueOf(Objects.requireNonNull(aircraftModelId, "aircraftModelId").trim());
        final EngineModelId eid = EngineModelId.valueOf(Objects.requireNonNull(engineModelId, "engineModelId").trim());

        final Optional<AircraftModel> aircraftModel = aircraftModels.findByID(aid);
        if (aircraftModel.isEmpty()) {
            throw new AircraftModelNotFoundException("Aircraft model not found: " + aid);
        }

        if (engineModels.findByModelId(eid).isEmpty()) {
            throw new EngineModelNotFoundException("Engine model not found: " + eid);
        }

        EngineModel engineModel = engineModels.findByModelId(eid).get();

        final AircraftModel model = aircraftModel.get();
        model.addEngineConfiguration(engineModel);

        return aircraftModels.save(model);
    }
}
