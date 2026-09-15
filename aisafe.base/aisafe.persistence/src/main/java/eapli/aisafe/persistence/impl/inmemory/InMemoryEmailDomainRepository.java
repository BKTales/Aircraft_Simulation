package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryEmailDomainRepository
    extends InMemoryDomainRepository<EmailDomain, String>
    implements EmailDomainRepository {

    @Override
    public Optional<EmailDomain> findByDomain(String domain) {
        return Optional.ofNullable(data().get(domain));
    }
}
