package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link AirportRepository}.
 *
 * @author aisafe team
 */
public class JpaAirportRepository
        extends JpaAutoTxRepository<Airport, AirportIATACode, AirportIATACode>
        implements AirportRepository {

    public JpaAirportRepository(final TransactionalContext autoTx) {
        super(autoTx, "iataCode");
    }

    public JpaAirportRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "iataCode");
    }

    @Override
    public Optional<Airport> findByIcaoCode(final AirportICAOCode icaoCode) {
        final Map<String, Object> params = new HashMap<>();
        params.put("code", icaoCode);
        return matchOne("e.icaoCode=:code", params);
    }
}
