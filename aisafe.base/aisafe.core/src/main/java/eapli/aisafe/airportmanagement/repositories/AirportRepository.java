package eapli.aisafe.airportmanagement.repositories;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

/**
 * Repository for {@link Airport} aggregate instances.
 *
 * @author aisafe team
 */
public interface AirportRepository extends DomainRepository<AirportIATACode, Airport> {

    /**
     * Finds an airport by its ICAO code.
     *
     * <p>Used to enforce the uniqueness of ICAO codes across all registered airports.</p>
     *
     * @param icaoCode the ICAO code to search for
     * @return an {@link Optional} containing the matching airport, or empty if none found
     */
    Optional<Airport> findByIcaoCode(AirportICAOCode icaoCode);
}
