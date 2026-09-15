package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryRouteRepository
        extends InMemoryDomainRepository<Route, RouteName>
        implements RouteRepository {
}
