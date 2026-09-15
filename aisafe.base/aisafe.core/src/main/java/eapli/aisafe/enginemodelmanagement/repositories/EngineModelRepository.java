package eapli.aisafe.enginemodelmanagement.repositories;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface EngineModelRepository extends DomainRepository<EngineModelId, EngineModel> {
    Optional<EngineModel> findByModelId(EngineModelId id);

    Optional<EngineModel> findByNameAndManufacturer(EngineName name, ManufacturerId manufacturerId);

    default boolean existsByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
        return findByNameAndManufacturer(name, manufacturerId).isPresent();
    }
}