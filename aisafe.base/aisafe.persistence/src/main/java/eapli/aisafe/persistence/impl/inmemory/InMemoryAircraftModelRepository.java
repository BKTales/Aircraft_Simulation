package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Objects;
import java.util.Optional;

public class InMemoryAircraftModelRepository
        extends InMemoryDomainRepository<AircraftModel, AircraftModelId>
        implements AircraftModelRepository {

    @Override
    public Optional<AircraftModel> findByID(final AircraftModelId id) {
        return matchOne(e -> e != null && e.identity().equals(id));
    }

    @Override
    public Optional<AircraftModel> existsByNameAndManufacturer(final ModelName name,
                                                               final ManufacturerId manufacturerId) {

        return data().values().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.name().equals(name))
                .filter(e -> e.manufacturer().equals(manufacturerId))
                .findFirst();
    }

    @Override
    public Iterable<AircraftModel> findByManufacturer(final ManufacturerId manufacturerId) {
        return match(e -> e != null && e.manufacturer().equals(manufacturerId));
    }
}