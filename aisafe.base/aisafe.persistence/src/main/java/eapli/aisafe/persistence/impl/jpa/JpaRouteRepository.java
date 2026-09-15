package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaRouteRepository
        extends JpaAutoTxRepository<Route, RouteName, RouteName>
        implements RouteRepository {

    public JpaRouteRepository(final TransactionalContext autoTx) {
        super(autoTx, "routeName");
    }

    public JpaRouteRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "routeName");
    }
}
