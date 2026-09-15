package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.application.EmailDomainAlreadyExistsException;
import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailDomainsBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailDomainsBootstrapper.class);

    private final EmailDomainRepository emailDomainRepo = PersistenceContext.repositories().emailDomains();

    @Override
    public boolean execute() {
        registerEmailDomain("aisafe.backoffice.com");
        registerEmailDomain("aisafe.admin.com");
        registerEmailDomain("aisafe.weather.com");
        return true;
    }

    private void registerEmailDomain(final String domain) {
        if (emailDomainRepo.ofIdentity(domain).isPresent()) {
            LOGGER.debug("Assuming email domain already exists: {}", domain);
            return;
        }

        final EmailDomain emailDomain = new EmailDomain(domain);
        try {
            emailDomainRepo.save(emailDomain);
        } catch (final IntegrityViolationException ex) {
            if (emailDomainRepo.ofIdentity(domain).isPresent()) {
                LOGGER.debug("Assuming email domain already exists: {}", domain);
                return;
            }
            throw ex;
        } catch (final EmailDomainAlreadyExistsException ex) {
            LOGGER.debug("Assuming email domain already exists: {}", domain);
        }
    }
}
