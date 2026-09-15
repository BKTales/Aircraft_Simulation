package eapli.aisafe.aircraftmodelmanagement.repositories;


import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AircraftModelRepository
        extends DomainRepository<AircraftModelId, AircraftModel> {

    Optional<AircraftModel> findByID(AircraftModelId id);

    Optional<AircraftModel> existsByNameAndManufacturer(ModelName name,
                                                        ManufacturerId manufacturerId);

    Iterable<AircraftModel> findByManufacturer(ManufacturerId manufacturerId);
}