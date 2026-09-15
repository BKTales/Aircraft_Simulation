package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaManufacturerRepository
        extends JpaAutoTxRepository<Manufacturer, ManufacturerId, ManufacturerId>
        implements ManufacturerRepository {

    JpaManufacturerRepository(final TransactionalContext autoTx) {
        super(autoTx, "identity");
    }

    JpaManufacturerRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "identity");
    }

    @Override
    public Optional<Manufacturer> findById(final ManufacturerId id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return matchOne("e.identity=:id", params);
    }
}

