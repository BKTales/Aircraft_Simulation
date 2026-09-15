package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaEngineModelRepository
        extends JpaAutoTxRepository<EngineModel, EngineModelId, EngineModelId>
        implements EngineModelRepository {

    JpaEngineModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "identity");
    }

    JpaEngineModelRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "identity");
    }

    @Override
    public Optional<EngineModel> findByModelId(final EngineModelId id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return matchOne("e.identity=:id", params);
    }

    @Override
    public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("mid", manufacturerId);
        return matchOne("e.name=:name AND e.manufacturer.identity=:mid", params);
    }
}

