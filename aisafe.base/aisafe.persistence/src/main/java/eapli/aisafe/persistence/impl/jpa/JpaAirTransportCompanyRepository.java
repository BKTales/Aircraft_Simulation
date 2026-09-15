package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaAirTransportCompanyRepository
        extends JpaAutoTxRepository<AirTransportCompany, IATACode, IATACode>
        implements AirTransportCompanyRepository {

    JpaAirTransportCompanyRepository(final TransactionalContext autoTx) {
        super(autoTx, "iataCode");
    }

    JpaAirTransportCompanyRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "iataCode");
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(final IATACode code) {
        final Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        return matchOne("e.iataCode=:code", params);
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(final ICAOCode code) {
        final Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        return matchOne("e.icaoCode=:code", params);
    }
}
