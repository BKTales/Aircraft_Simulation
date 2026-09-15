package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryManufacturerRepository
        extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
        implements ManufacturerRepository {

    @Override
    public Optional<Manufacturer> findById(final ManufacturerId id) {
        return Optional.ofNullable(data().get(id));
    }
}

