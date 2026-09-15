package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaCompanyCollaboratorUserRepository
        extends JpaAutoTxRepository<CompanyCollaboratorUser, AISafeUserId, AISafeUserId>
        implements CompanyCollaboratorUserRepository {

    JpaCompanyCollaboratorUserRepository(final TransactionalContext autoTx) {
        super(autoTx, "email");
    }

    JpaCompanyCollaboratorUserRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "email");
    }


    @Override
    public Iterable<CompanyCollaboratorUser> findATCCByCompanyAndActive(final AirTransportCompany airTransportCompany) {
        final Map<String, Object> params = new HashMap<>();
        params.put("active", true);
        params.put("company", airTransportCompany);
        params.put("role", CompanyCollaboratorRoles.ATCC);

        return match("e.systemUser.active = :active AND e.airTransportCompany = :company AND e.role = :role ", params);
    }

    @Override
    public Optional<CompanyCollaboratorUser> findByUsername(final Username username) {
        final Map<String, Object> params = new HashMap<>();
        params.put("u", username);
        return matchOne("e.systemUser.username = :u", params);
    }
}
