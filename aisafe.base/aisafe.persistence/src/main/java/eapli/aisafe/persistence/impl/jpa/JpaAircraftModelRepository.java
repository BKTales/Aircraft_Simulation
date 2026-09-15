package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaAircraftModelRepository
        extends JpaAutoTxRepository<AircraftModel, AircraftModelId, AircraftModelId>
        implements AircraftModelRepository {

    JpaAircraftModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "modelCode");
    }

    JpaAircraftModelRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "modelCode");
    }

    @Override
    public Optional<AircraftModel> findByID(final AircraftModelId id) {

        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return matchOne(
                "e.modelCode = :id",
                params
        );
    }

    @Override
    public Optional<AircraftModel> existsByNameAndManufacturer(final ModelName name,
                                                               final ManufacturerId manufacturerId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("manufacturerId", manufacturerId);

        return matchOne(
                "e.name = :name AND e.manufacturer.identity = :manufacturerId",
                params
        );
    }

    @Override
    public Iterable<AircraftModel> findByManufacturer(final ManufacturerId manufacturerId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("manufacturerId", manufacturerId);

        return match(
                "e.manufacturer.identity = :manufacturerId",
                params
        );
    }
}
