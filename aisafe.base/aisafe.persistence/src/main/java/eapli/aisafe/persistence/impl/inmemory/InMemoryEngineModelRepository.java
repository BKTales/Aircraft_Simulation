package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryEngineModelRepository
        extends InMemoryDomainRepository<EngineModel, EngineModelId>
        implements EngineModelRepository {

    @Override
    public Optional<EngineModel> findByModelId(final EngineModelId id) {
        return Optional.ofNullable(data().get(id));
    }

    @Override
    public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
        return matchOne(e -> e != null
                && name.equals(e.name())
                && manufacturerId.equals(e.manufacturerId()));
    }
}

