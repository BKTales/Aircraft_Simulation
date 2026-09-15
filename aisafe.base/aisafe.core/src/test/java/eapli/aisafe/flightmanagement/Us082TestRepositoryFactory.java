package eapli.aisafe.flightmanagement;

import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.DeactivateRouteTestRepositories;
import eapli.aisafe.weatherdata.TestWeatherDataRepositoryFactory;
import eapli.framework.domain.repositories.TransactionalContext;

/**
 * Minimal {@link eapli.aisafe.infrastructure.persistence.RepositoryFactory} for US082 default-constructor coverage.
 */
public class Us082TestRepositoryFactory extends TestWeatherDataRepositoryFactory {

    private final FlightRepository flights = DeactivateRouteTestRepositories.newFlightRepository();

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return flights;
    }

    @Override
    public FlightRepository flights() {
        return flights;
    }
}
