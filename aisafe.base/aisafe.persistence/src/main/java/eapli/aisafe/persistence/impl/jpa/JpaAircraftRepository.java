package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;

public class JpaAircraftRepository
        extends JpaAutoTxRepository<Aircraft, AircraftRegistration, AircraftRegistration>
        implements AircraftRepository {

    public JpaAircraftRepository(final TransactionalContext autoTx) {
        super(autoTx, "registration");
    }

    public JpaAircraftRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "registration");
    }

    @Override
    public Iterable<Aircraft> findActiveByOwnerCompany(final IATACode ownerIata) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iata", ownerIata.toString());
        params.put("active", OperationalStatus.ACTIVE);
        return match("e.ownerCompanyIata.code = :iata AND e.operationalStatus = :active", params);
    }

    @Override
    public Iterable<Aircraft> findByOwnerCompany(final IATACode ownerIata) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iata", ownerIata.toString());
        return match("e.ownerCompanyIata.code = :iata", params);
    }
}
