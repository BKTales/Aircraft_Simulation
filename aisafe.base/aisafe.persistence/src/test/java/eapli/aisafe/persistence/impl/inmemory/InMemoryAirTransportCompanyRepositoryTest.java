package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAirTransportCompanyRepositoryTest {

    @Test
    void ensureFindAndExistsWorkInMemory() {
        final var repo = new InMemoryAirTransportCompanyRepository();
        final var company = new AirTransportCompany(
                CompanyName.valueOf("TAP Air Portugal"),
                IATACode.valueOf("TP"),
                ICAOCode.valueOf("TAP")
        );

        repo.save(company);

        assertTrue(repo.findByIataCode(IATACode.valueOf("TP")).isPresent());
        assertTrue(repo.findByIcaoCode(ICAOCode.valueOf("TAP")).isPresent());
        assertTrue(repo.existsByIataCode(IATACode.valueOf("TP")));
        assertTrue(repo.existsByIcaoCode(ICAOCode.valueOf("TAP")));


        assertFalse(repo.findByIataCode(IATACode.valueOf("AF")).isPresent());
        assertFalse(repo.findByIcaoCode(ICAOCode.valueOf("AFR")).isPresent());
    }
}
