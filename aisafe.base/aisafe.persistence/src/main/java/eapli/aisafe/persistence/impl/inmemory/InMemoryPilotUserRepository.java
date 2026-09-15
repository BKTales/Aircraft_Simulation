package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryPilotUserRepository extends InMemoryDomainRepository<PilotUser, AISafeUserId>
        implements PilotUserRepository {


    @Override
    public Optional<PilotUser> findByUsername(final Username username) {
        return data().values().stream()
                .filter(c -> c.systemUser().username().equals(username))
                .findFirst();
    }

    @Override
    public Iterable<PilotUser> findPilotByCompanyAndActive(AirTransportCompany airTransportCompany) {
        return data().values().stream()
                .filter(c -> c.airTransportCompany().identity().equals(airTransportCompany.identity()))
                .filter(c -> c.systemUser().isActive())
                .toList();
    }

    @Override
    public Optional<PilotUser> findByEmail(final EmailAddress email) {
        return data().values().stream()
                .filter(p -> p.systemUser().email().equals(email))
                .findFirst();
    }

    @Override
    public Optional<PilotUser> findByEmailWithLock(final EmailAddress email,
                                                   final AirTransportCompany company) {
        return data().values().stream()
                .filter(p -> p.systemUser().email().equals(email))
                .filter(p -> p.airTransportCompany().identity().equals(company.identity()))
                .findFirst();
    }
}
