package eapli.aisafe.flightmanagement;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.weatherdata.TestWeatherDataRepositoryFactory;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

/**
 * Minimal {@link eapli.aisafe.infrastructure.persistence.RepositoryFactory} for US112 controller default-constructor coverage.
 */
public class Us112TestRepositoryFactory extends TestWeatherDataRepositoryFactory {

    private final FlightControlOperatorUserRepository flightOperators = new InMemoryFlightOperatorRepository();

    @Override
    public FlightControlOperatorUserRepository flightOperators(final TransactionalContext autoTx) {
        return flightOperators;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
        return flightOperators;
    }

    static final class InMemoryFlightOperatorRepository
            extends InMemoryDomainRepository<FlightControlOperatorUser, AISafeUserId>
            implements FlightControlOperatorUserRepository {

        @Override
        public Iterable<FlightControlOperatorUser> findByAreaAndActive(final AirControlArea airControlArea) {
            return java.util.List.of();
        }

        @Override
        public Optional<FlightControlOperatorUser> findByUsername(final Username username) {
            return Optional.empty();
        }
    }
}
