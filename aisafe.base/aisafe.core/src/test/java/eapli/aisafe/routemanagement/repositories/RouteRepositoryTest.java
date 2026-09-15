package eapli.aisafe.routemanagement.repositories;

import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteRepositoryTest {

    private FakeRouteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FakeRouteRepository();
    }

    @Test
    void existsByNameReturnsTrueWhenPresent() {
        final Route route = Route.charterRoute(
                RouteName.valueOf("TP123"),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.charterSchedule());
        repository.save(route);

        assertTrue(repository.existsByName(RouteName.valueOf("TP123")));
        assertFalse(repository.existsByName(RouteName.valueOf("TP999")));
    }

    @Test
    void findByCompanyIATACodeReturnsMatchingRoutes() {
        final Route tpRoute = Route.regularRoute(
                RouteName.valueOf("TP100"),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule());
        final Route frRoute = Route.regularRoute(
                RouteName.valueOf("FR200"),
                RouteTestFixtures.company("Ryanair", "FR", "RYR"),
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.mondaySchedule());
        repository.save(tpRoute);
        repository.save(frRoute);

        final List<Route> tpRoutes = toList(repository.findByCompanyIATACode(IATACode.valueOf("TP")));
        assertTrue(tpRoutes.stream().anyMatch(r -> "TP100".equals(r.identity().toString())));
        assertTrue(tpRoutes.stream().noneMatch(r -> "FR200".equals(r.identity().toString())));
        assertTrue(tpRoutes.stream().allMatch(r -> r.companyIATACode().equals(IATACode.valueOf("TP"))));
    }

    @Test
    void findByCompanyDelegatesToIataLookup() {
        final Route route = Route.charterRoute(
                RouteName.valueOf("TP123"),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.charterSchedule());
        repository.save(route);

        final List<Route> byCompany = toList(repository.findByCompany(RouteTestFixtures.COMPANY_TP));
        assertTrue(byCompany.stream().anyMatch(r -> "TP123".equals(r.identity().toString())));
    }

    @Test
    void findActiveByCompanyExcludesDeactivatedRoutes() {
        final Route active = Route.charterRoute(
                RouteName.valueOf("TP123"),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.charterSchedule());
        final Route inactive = Route.regularRouteDeactivated(
                RouteName.valueOf("TP888"),
                RouteTestFixtures.COMPANY_TP,
                RouteTestFixtures.AIRPORT_OPO,
                RouteTestFixtures.AIRPORT_LIS,
                RouteTestFixtures.mondaySchedule(),
                LocalDate.of(2026, 6, 1));
        repository.save(active);
        repository.save(inactive);

        final List<Route> result = toList(
                repository.findActiveByCompany(IATACode.valueOf("TP"), LocalDate.of(2026, 6, 1)));

        assertTrue(result.stream().anyMatch(r -> "TP123".equals(r.identity().toString())));
        assertTrue(result.stream().noneMatch(r -> "TP888".equals(r.identity().toString())));
    }

    @Test
    void findByCompanyIATACodeRejectsNull() {
        assertThrows(NullPointerException.class, () -> repository.findByCompanyIATACode(null));
        assertThrows(NullPointerException.class, () -> repository.findByCompany(null));
    }

    private static List<Route> toList(final Iterable<Route> routes) {
        final List<Route> list = new ArrayList<>();
        routes.forEach(list::add);
        return list;
    }

    private static final class FakeRouteRepository
            extends InMemoryDomainRepository<Route, RouteName>
            implements RouteRepository {
    }
}
