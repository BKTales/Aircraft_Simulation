package eapli.aisafe.usermanagement.repositories;

import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface EmailDomainRepository extends DomainRepository<String, EmailDomain> {
    Optional<EmailDomain> findByDomain(String domain);
}
