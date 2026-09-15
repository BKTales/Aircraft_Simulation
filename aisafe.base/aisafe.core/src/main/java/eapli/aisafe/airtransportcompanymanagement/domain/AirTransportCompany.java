package eapli.aisafe.airtransportcompanymanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;

import java.io.Serializable;

@Entity
public class AirTransportCompany implements AggregateRoot<IATACode>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private IATACode iataCode;

    @Version
    private Long version;

    @Embedded
    private CompanyName companyName;

    @Embedded
    private ICAOCode icaoCode;

    protected AirTransportCompany() {
        // for ORM
    }

    public AirTransportCompany(final CompanyName companyName, final IATACode iataCode, final ICAOCode icaoCode) {
        if (companyName == null || iataCode == null || icaoCode == null) {
            throw new IllegalArgumentException("Company name, IATA code and ICAO code are required.");
        }
        this.companyName = companyName;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public IATACode identity() {
        return this.iataCode;
    }

    public CompanyName companyName() {
        return this.companyName;
    }

    public ICAOCode icaoCode() {
        return this.icaoCode;
    }
}
