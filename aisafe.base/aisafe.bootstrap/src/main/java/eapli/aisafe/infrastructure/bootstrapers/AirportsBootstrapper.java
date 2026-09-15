package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.airportmanagement.application.AirportIATACodeAlreadyExistsException;
import eapli.aisafe.airportmanagement.application.AirportICAOCodeAlreadyExistsException;
import eapli.aisafe.airportmanagement.application.AirportService;
import eapli.aisafe.airportmanagement.application.NoAreaFoundForCoordinatesException;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps sample airports for development and demo purposes.
 *
 * <p>Each airport's air control area is resolved automatically from its coordinates.
 * This bootstrapper is idempotent: duplicate airports are silently skipped.</p>
 */
public class AirportsBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsBootstrapper.class);

    @Override
    public boolean execute() {
        final AirportService service = new AirportService(
                PersistenceContext.repositories().airports(),
                PersistenceContext.repositories().airControlArea());

        registerAirport(service, "LIS", "LPPT", 38.7756, -9.1354, 113.0);
        registerAirport(service, "OPO", "LPPR", 41.2481, -8.6814, 69.0);
        registerAirport(service, "FAO", "LPFR", 37.0144, -7.9659, 8.0);
        registerAirport(service, "FNC", "LPMA", 32.6949, -16.7745, 58.0);
        registerAirport(service, "PDL", "LPPD", 37.7412, -25.6979, 79.0);
        registerAirport(service, "MAD", "LEMD", 40.4936, -3.5668, 610.0);
        return true;
    }

    private void registerAirport(final AirportService service,
                                 final String iata, final String icao,
                                 final double lat, final double lon, final double elev) {
        try {
            service.registerAirport(iata, icao, lat, lon, elev);
        } catch (final AirportIATACodeAlreadyExistsException | AirportICAOCodeAlreadyExistsException ex) {
            LOGGER.debug("Assuming airport already exists: {}", ex.getMessage());
        } catch (final NoAreaFoundForCoordinatesException | IllegalArgumentException ex) {
            LOGGER.warn("Could not register airport '{}/{}': {}", iata, icao, ex.getMessage());
        }
    }
}
