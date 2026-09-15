package eapli.aisafe.persistence.impl.inmemory;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryCompanyCollaboratorUserRepository
        extends InMemoryDomainRepository<CompanyCollaboratorUser, AISafeUserId>
        implements CompanyCollaboratorUserRepository {


    @Override
    public Iterable<CompanyCollaboratorUser> findATCCByCompanyAndActive(final AirTransportCompany airTransportCompany) {
        return data().values().stream()
                .filter(c -> c.airTransportCompany().equals(airTransportCompany))
                .filter(c -> c.systemUser().isActive())
                .toList();
    }


    @Override
    public Optional<CompanyCollaboratorUser> findByUsername(final Username username) {
        return data().values().stream()
                .filter(c -> c.systemUser().username().equals(username))
                .findFirst();
    }
}
