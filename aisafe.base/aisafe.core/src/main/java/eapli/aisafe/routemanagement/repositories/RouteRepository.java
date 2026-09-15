package eapli.aisafe.routemanagement.repositories;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface RouteRepository extends DomainRepository<RouteName, Route> {

    default boolean existsByName(final RouteName routeName) {
        return ofIdentity(routeName).isPresent();
    }

    default Iterable<Route> findByCompanyIATACode(final IATACode companyIATACode) {
        Objects.requireNonNull(companyIATACode, "companyIATACode");
        final List<Route> result = new ArrayList<>();
        for (final Route route : findAll()) {
            if (route.companyIATACode().equals(companyIATACode)) {
                result.add(route);
            }
        }
        return result;
    }

    default Iterable<Route> findByCompany(final AirTransportCompany company) {
        Objects.requireNonNull(company, "company");
        return findByCompanyIATACode(company.identity());
    }

    default Iterable<Route> findActiveByCompany(final IATACode companyIATACode, final LocalDate asOf) {
        Objects.requireNonNull(companyIATACode, "companyIATACode");
        Objects.requireNonNull(asOf, "asOf");
        final List<Route> result = new ArrayList<>();
        for (final Route route : findByCompanyIATACode(companyIATACode)) {
            if (route.isActiveOn(asOf)) {
                result.add(route);
            }
        }
        return result;
    }
}

