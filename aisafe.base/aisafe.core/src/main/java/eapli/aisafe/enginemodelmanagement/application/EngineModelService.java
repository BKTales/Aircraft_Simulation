package eapli.aisafe.enginemodelmanagement.application;

import eapli.aisafe.enginemodelmanagement.domain.*;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;

import java.util.Objects;

public class EngineModelService {

    private final EngineModelRepository engineModels;
    private final ManufacturerRepository manufacturers;

    public EngineModelService(final EngineModelRepository engineModels,
                              final ManufacturerRepository manufacturers) {
        if (engineModels == null || manufacturers == null) {
            throw new IllegalArgumentException("Engine model and manufacturer repositories are required.");
        }
        this.engineModels = engineModels;
        this.manufacturers = manufacturers;
    }

    public EngineModel createEngineModel(final String name,
                                         final String manufacturerId,
                                         final String motorization,
                                         final double thrustAtStatic,
                                         final double thrustAtCruise,
                                         final String fuelType,
                                         final double tsfc) {

        final var mid = ManufacturerId.valueOf(Objects.requireNonNull(manufacturerId, "manufacturerId").trim());
        final Manufacturer manufacturer = manufacturers.findById(mid)
                .orElseThrow(() -> new ManufacturerNotFoundException("Manufacturer not found: " + mid));

        final var engineName = EngineName.valueOf(name);
        if (engineModels.existsByNameAndManufacturer(engineName, mid)) {
            throw new EngineModelAlreadyExistsException("Engine model already registered for this manufacturer.");
        }

        final var id = EngineModelId.valueOf(mid + "-" + engineName.toString());
        final var model = new EngineModel(
                id,
                engineName,
                TSFC.valueOf(tsfc),
                FuelType.valueOf(Objects.requireNonNull(fuelType, "fuelType").trim().toUpperCase()),
                ThrustProfile.valueOf(thrustAtStatic, thrustAtCruise),
                manufacturer,
                MotorizationType.fromLabel(motorization)
        );

        try {
            return engineModels.save(model);
        } catch (final IntegrityViolationException ex) {
            throw new EngineModelAlreadyExistsException("Engine model already exists.");
        }
    }
}

