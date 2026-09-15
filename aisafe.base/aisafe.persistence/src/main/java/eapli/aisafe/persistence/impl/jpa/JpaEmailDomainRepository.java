package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaEmailDomainRepository
    extends JpaAutoTxRepository<EmailDomain, String, String>
    implements EmailDomainRepository {

    JpaEmailDomainRepository(final TransactionalContext autoTx) {
        super(autoTx, "domain");
    }

    JpaEmailDomainRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "domain");
    }

    @Override
    public Optional<EmailDomain> findByDomain(String domain) {
        final Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        return matchOne("e.domain=:domain", params);
    }
}
