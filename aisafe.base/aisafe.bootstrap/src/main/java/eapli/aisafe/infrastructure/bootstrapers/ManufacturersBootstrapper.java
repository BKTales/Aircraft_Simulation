package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufacturersBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManufacturersBootstrapper.class);

    @Override
    public boolean execute() {
        final ManufacturerRepository repo = PersistenceContext.repositories().manufacturers();
        save(repo, "MAN01", "CFM International", "FR");
        save(repo, "MAN02", "Pratt & Whitney", "US");
        save(repo, "MAN03", "Rolls-Royce", "GB");
        save(repo, "MAN04", "GE Aerospace", "US");
        save(repo, "MAN05", "Safran Aircraft Engines", "FR");
        return true;
    }

    private void save(final ManufacturerRepository repo, final String id, final String name, final String cc) {
        try {
            repo.save(new Manufacturer(ManufacturerId.valueOf(id), new ManufacturerName(name), new CountryCode(cc)));
        } catch (final IntegrityViolationException | ConcurrencyException ex) {
            LOGGER.debug("Assuming manufacturer already exists: {}", id);
        } catch (final jakarta.persistence.RollbackException ex) {
            LOGGER.debug("Assuming manufacturer already exists (rollback): {}", id);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Could not bootstrap manufacturer {}: {}", id, ex.getMessage());
        }
    }
}
