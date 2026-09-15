package eapli.aisafe.routemanagement;

import eapli.aisafe.airportmanagement.TestAirportControllersRepositoryFactory;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link RepositoryFactory} for tests covering {@link eapli.aisafe.routemanagement.application.CreateRouteController}
 * no-arg constructor (requires non-null route, airport and flight repositories).
 */
public class TestRouteControllersRepositoryFactory extends TestAirportControllersRepositoryFactory {

    private final RouteRepository routes = new InMemoryRouteRepository();
    private final FlightRepository flights = new InMemoryFlightRepository();
    private final CompanyCollaboratorUserRepository collaborators = new InMemoryCompanyCollaboratorUserRepository();

    @Override
    public CompanyCollaboratorUserRepository collaborators(final TransactionalContext autoTx) {
        return collaborators;
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators() {
        return collaborators;
    }

    @Override
    public RouteRepository routes(final TransactionalContext autoTx) {
        return routes;
    }

    @Override
    public RouteRepository routes() {
        return routes;
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return flights;
    }

    @Override
    public FlightRepository flights() {
        return flights;
    }

    static final class InMemoryRouteRepository
            extends InMemoryDomainRepository<Route, RouteName>
            implements RouteRepository {
    }

    static final class InMemoryFlightRepository
            extends InMemoryDomainRepository<Flight, FlightDesignator>
            implements FlightRepository {

        @Override
        public boolean existsPendingFlightForAircraft(final String aircraftRegistration, final LocalDateTime asOf) {
            return false;
        }

        @Override
        public boolean existsActiveFlightForPilot(final SystemUser pilotSystemUser, final LocalDateTime asOf) {
            return false;
        }

        @Override
        public List<Flight> findScheduledWithFlightPlan(final LocalDateTime start, final LocalDateTime end) {
            return List.of();
        }

        @Override
        public java.util.List<eapli.aisafe.flightmanagement.application.FlightValidationPreview> findDraftFlightsForPilot(final SystemUser pilotSystemUser) {
            return java.util.List.of();
        }

        @Override
        public boolean existsPlannedFlightOnRouteAfter(final RouteName routeName, final LocalDate deactivationDate) {
            final String normalizedRoute = routeName.toString().trim().toUpperCase();
            for (final Flight f : findAll()) {
                if (matchesPlannedOnRoute(normalizedRoute, f, deactivationDate)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean matchesPlannedOnRoute(
                final String normalizedRouteName, final Flight f, final LocalDate deactivationDate) {
            if (f.routeName() == null || f.schedule() == null || deactivationDate == null) {
                return false;
            }
            if (!normalizedRouteName.equals(f.routeName().trim().toUpperCase())) {
                return false;
            }
            if (f.schedule().scheduledDeparture().toLocalDate().isBefore(deactivationDate)) {
                return false;
            }
            if (f.flightPlan() == null) {
                return true;
            }
            final FlightPlanStatus status = f.flightPlan().status();
            return status == FlightPlanStatus.DRAFT
                    || status == FlightPlanStatus.SUBMITTED_FOR_SIMULATION
                    || status == FlightPlanStatus.SIM_APPROVED;
        }
    }

    private static final class InMemoryCompanyCollaboratorUserRepository
            extends InMemoryDomainRepository<CompanyCollaboratorUser, AISafeUserId>
            implements CompanyCollaboratorUserRepository {

        @Override
        public Iterable<CompanyCollaboratorUser> findATCCByCompanyAndActive(final AirTransportCompany airTransportCompany) {
            return java.util.List.of();
        }

        @Override
        public Optional<CompanyCollaboratorUser> findByUsername(final Username username) {
            return java.util.stream.StreamSupport.stream(findAll().spliterator(), false)
                    .filter(c -> c.systemUser().identity().equals(username))
                    .findFirst();
        }
    }
}
