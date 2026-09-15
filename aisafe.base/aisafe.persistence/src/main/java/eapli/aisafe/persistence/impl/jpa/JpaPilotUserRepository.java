package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import jakarta.persistence.LockModeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class JpaPilotUserRepository
        extends JpaAutoTxRepository<PilotUser, AISafeUserId, AISafeUserId>
        implements eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository {

    JpaPilotUserRepository(final TransactionalContext autoTx) {
        super(autoTx, "email");
    }

    JpaPilotUserRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "email");
    }


    @Override
    public Iterable<PilotUser> findPilotByCompanyAndActive(final AirTransportCompany airTransportCompany) {
        final Map<String, Object> params = new HashMap<>();
        params.put("active", true);
        params.put("iata", airTransportCompany.identity());

        return match(
                "e.systemUser.active = :active AND e.airTransportCompany.iataCode = :iata",
                params);
    }

    @Override
    public Optional<PilotUser> findByUsername(final Username username) {
        final Map<String, Object> params = new HashMap<>();
        params.put("u", username);
        return matchOne("e.systemUser.username = :u", params);
    }

    @Override
    public Optional<PilotUser> findByEmail(final EmailAddress email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        final List<PilotUser> matches = match("e.systemUser.email = :email", params);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Override
    public Optional<PilotUser> findByEmailWithLock(final EmailAddress email,
                                                   final AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("iata", company.identity());
        return matchOne(
                LockModeType.PESSIMISTIC_WRITE,
                "e.systemUser.email = :email AND e.airTransportCompany.iataCode = :iata",
                params);
    }
}
