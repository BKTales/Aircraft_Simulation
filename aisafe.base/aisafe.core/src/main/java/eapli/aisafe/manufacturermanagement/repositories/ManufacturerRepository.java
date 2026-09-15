package eapli.aisafe.manufacturermanagement.repositories;


import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface ManufacturerRepository extends DomainRepository<ManufacturerId, Manufacturer> {
    Optional<Manufacturer> findById(ManufacturerId id);
}