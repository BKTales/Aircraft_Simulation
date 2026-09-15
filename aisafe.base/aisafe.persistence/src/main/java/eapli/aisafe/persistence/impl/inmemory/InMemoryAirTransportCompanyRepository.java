package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirTransportCompanyRepository
        extends InMemoryDomainRepository<AirTransportCompany, IATACode>
        implements AirTransportCompanyRepository {

    @Override
    public Optional<AirTransportCompany> findByIataCode(final IATACode code) {
        return Optional.ofNullable(data().get(code));
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(final ICAOCode code) {
        return matchOne(company -> company.icaoCode().equals(code));
    }
}
