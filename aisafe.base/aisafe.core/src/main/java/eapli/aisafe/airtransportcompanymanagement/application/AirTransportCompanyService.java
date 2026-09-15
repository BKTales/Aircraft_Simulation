package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;

public class AirTransportCompanyService {

    private final AirTransportCompanyRepository repository;

    public AirTransportCompanyService(final AirTransportCompanyRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null.");
        }
        this.repository = repository;
    }

    public AirTransportCompany registerCompany(final String name, final String iataCode, final String icaoCode) {
        final CompanyName companyName = CompanyName.valueOf(name);
        final IATACode iata = IATACode.valueOf(iataCode);
        final ICAOCode icao = ICAOCode.valueOf(icaoCode);

        if (repository.existsByIataCode(iata)) {
            throw new IATACodeAlreadyExistsException(iata.toString());
        }
        if (repository.existsByIcaoCode(icao)) {
            throw new ICAOCodeAlreadyExistsException(icao.toString());
        }

        final AirTransportCompany company = new AirTransportCompany(companyName, iata, icao);
        try {
            return repository.save(company);
        } catch (final IntegrityViolationException ex) {
            if (repository.existsByIataCode(iata)) {
                throw new IATACodeAlreadyExistsException(iata.toString());
            }
            if (repository.existsByIcaoCode(icao)) {
                throw new ICAOCodeAlreadyExistsException(icao.toString());
            }
            throw ex;
        }
    }
}
