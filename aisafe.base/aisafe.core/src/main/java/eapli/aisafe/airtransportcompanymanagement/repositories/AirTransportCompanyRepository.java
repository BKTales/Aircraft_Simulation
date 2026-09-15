package eapli.aisafe.airtransportcompanymanagement.repositories;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirTransportCompanyRepository extends DomainRepository<IATACode, AirTransportCompany> {

    Optional<AirTransportCompany> findByIataCode(IATACode code);

    Optional<AirTransportCompany> findByIcaoCode(ICAOCode code);

    default boolean existsByIataCode(final IATACode code) {
        return findByIataCode(code).isPresent();
    }

    default boolean existsByIcaoCode(final ICAOCode code) {
        return findByIcaoCode(code).isPresent();
    }
}
