package eapli.aisafe.infrastructure.bootstrapers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.aisafe.airtransportcompanymanagement.application.AirTransportCompanyService;
import eapli.aisafe.airtransportcompanymanagement.application.ICAOCodeAlreadyExistsException;
import eapli.aisafe.airtransportcompanymanagement.application.IATACodeAlreadyExistsException;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;

/**
 * Bootstraps a minimal set of air transport companies for development and demos.
 * <p>
 * This bootstrapper is intended to be idempotent: re-running it should not fail if
 * the companies already exist.
 * </p>
 */
public class AirTransportCompaniesBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirTransportCompaniesBootstrapper.class);

    private final AirTransportCompanyService service = new AirTransportCompanyService(
            PersistenceContext.repositories().airTransportCompanies());

    @Override
    public boolean execute() {
        registerCompany("TAP Air Portugal", "TP", "TPA");
        registerCompany("Ryanair", "FR", "RYR");
        registerCompany("Lufthansa", "LH", "DLH");
        registerCompany("Vueling", "VY", "VLG");
        registerCompany("Iberia", "IB", "IBE");
        return true;
    }

    private void registerCompany(final String name, final String iata, final String icao) {
        try {
            service.registerCompany(name, iata, icao);
        } catch (final IATACodeAlreadyExistsException | ICAOCodeAlreadyExistsException ex) {
            LOGGER.debug("Assuming air transport company already exists: {}", ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Invalid air transport company seed data for '{}' ({}/{}): {}", name, iata, icao, ex.getMessage());
        }
    }
}
