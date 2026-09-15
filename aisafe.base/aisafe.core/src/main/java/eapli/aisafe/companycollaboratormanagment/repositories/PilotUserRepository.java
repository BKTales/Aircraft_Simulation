package eapli.aisafe.companycollaboratormanagment.repositories;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Username;

import java.util.Optional;

public interface PilotUserRepository extends DomainRepository<AISafeUserId, PilotUser> {

    public Iterable<PilotUser> findPilotByCompanyAndActive(final AirTransportCompany airTransportCompany);

    Optional<PilotUser> findByUsername(Username username);

    Optional<PilotUser> findByEmail(EmailAddress email);

    Optional<PilotUser> findByEmailWithLock(EmailAddress email, AirTransportCompany company);

}
