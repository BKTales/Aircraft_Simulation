package eapli.aisafe.airportmanagement.repositories;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * In-memory implementation of {@link AirportRepository}, intended for testing.
 *
 * @author aisafe team
 */
public class InMemoryAirportRepository
        extends InMemoryDomainRepository<Airport, AirportIATACode>
        implements AirportRepository {

    @Override
    public Optional<Airport> findByIcaoCode(final AirportICAOCode icaoCode) {
        return matchOne(new Predicate<Airport>() {
            @Override
            public boolean test(final Airport airport) {
                return airport.icaoCode().equals(icaoCode);
            }
        });
    }
}
