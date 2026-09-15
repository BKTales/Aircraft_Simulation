package eapli.aisafe.aircraftmanagement;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal {@link RepositoryFactory} for aircraft-management tests.
 */
public class TestAircraftRepositoryFactory implements RepositoryFactory {

    private final IsolatedFlightRepository flights = new IsolatedFlightRepository();
    private final IsolatedAircraftRepository aircraft = new IsolatedAircraftRepository();
    private final InMemoryAircraftModelRepository aircraftModels = new InMemoryAircraftModelRepository();
    private final InMemoryCompanyCollaboratorUserRepository collaborators = new InMemoryCompanyCollaboratorUserRepository();

    public AircraftRepository aircraft() {
        return aircraft;
    }

    public FlightRepository flights() {
        return flights;
    }

    public CompanyCollaboratorUserRepository collaborators() {
        return collaborators;
    }

    @Override
    public PilotUserRepository pilots(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public PilotUserRepository pilots() {
        return null;
    }

    public AircraftModelRepository aircraftModels() {
        return aircraftModels;
    }

    /**
     * Per-factory aircraft store (framework {@link InMemoryDomainRepository} uses JVM-wide static storage).
     */
    static final class IsolatedAircraftRepository implements AircraftRepository {
        private final Map<AircraftRegistration, Aircraft> byId = new HashMap<>();

        @Override
        public Aircraft save(final Aircraft entity) {
            byId.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<Aircraft> ofIdentity(final AircraftRegistration id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Iterable<Aircraft> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public boolean containsOfIdentity(final AircraftRegistration id) {
            return byId.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final AircraftRegistration entityId) {
            byId.remove(entityId);
        }

        @Override
        public void delete(final Aircraft entity) {
            deleteOfIdentity(entity.identity());
        }

        @Override
        public long count() {
            return byId.size();
        }
    }

    static final class IsolatedFlightRepository implements FlightRepository {
        private final Map<FlightDesignator, Flight> byId = new HashMap<>();

        @Override
        public boolean existsPendingFlightForAircraft(final String aircraftRegistration, final LocalDateTime asOf) {
            final String reg = new AircraftRegistration(aircraftRegistration).toString();
            return byId.values().stream().anyMatch(f -> matchesPending(reg, f, asOf));
        }

        private static boolean matchesPending(final String normalizedReg, final Flight f, final LocalDateTime asOf) {
            if (f.aircraftRegistration() == null || f.schedule() == null) {
                return false;
            }
            final String fr;
            try {
                fr = new AircraftRegistration(f.aircraftRegistration()).toString();
            } catch (final RuntimeException ex) {
                return false;
            }
            return normalizedReg.equals(fr) && !f.schedule().scheduledDeparture().isBefore(asOf);
        }

        @Override
        public boolean existsActiveFlightForPilot(final SystemUser pilotSystemUser, final LocalDateTime asOf) {
            return byId.values().stream().anyMatch(f -> matchesBlockingPlanForPilot(pilotSystemUser, f, asOf));
        }

        private static boolean matchesBlockingPlanForPilot(
                final SystemUser pilotSystemUser, final Flight f, final LocalDateTime asOf) {
            if (f.pilot() == null || f.flightPlan() == null || f.schedule() == null || asOf == null) {
                return false;
            }
            if (!f.pilot().systemUser().equals(pilotSystemUser)) {
                return false;
            }
            if (f.schedule().scheduledDeparture().isBefore(asOf)) {
                return false;
            }
            final FlightPlanStatus status = f.flightPlan().status();
            return status == FlightPlanStatus.DRAFT
                    || status == FlightPlanStatus.SUBMITTED_FOR_SIMULATION
                    || status == FlightPlanStatus.SIM_APPROVED;
        }

        @Override
        public List<Flight> findScheduledWithFlightPlan(final LocalDateTime start, final LocalDateTime end) {
            return byId.values().stream()
                    .filter(f -> f.flightPlan() != null && f.schedule() != null)
                    .filter(f -> f.flightPlan().jsonContent() != null && !f.flightPlan().jsonContent().isBlank())
                    .filter(f -> !f.schedule().scheduledDeparture().isAfter(end))
                    .filter(f -> !f.schedule().scheduledArrival().isBefore(start))
                    .toList();
        }

        @Override
        public java.util.List<eapli.aisafe.flightmanagement.application.FlightValidationPreview> findDraftFlightsForPilot(final SystemUser pilotSystemUser) {
            return byId.values().stream()
                    .filter(f -> f.pilot() != null && f.flightPlan() != null)
                    .filter(f -> f.pilot().systemUser().equals(pilotSystemUser))
                    .filter(f -> f.flightPlan().status() == FlightPlanStatus.DRAFT)
                    .map(f -> new eapli.aisafe.flightmanagement.application.FlightValidationPreview(
                            f.identity().toString(), f.routeName(), null, null,
                            f.aircraftRegistration(), f.flightPlan().status(),
                            f.schedule() != null ? f.schedule().scheduledDeparture() : null, null))
                    .toList();
        }

        @Override
        public boolean existsPlannedFlightOnRouteAfter(
                final eapli.aisafe.routemanagement.domain.RouteName routeName, final LocalDate deactivationDate) {
            return false;
        }

        @Override
        public Flight save(final Flight entity) {
            byId.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<Flight> ofIdentity(final FlightDesignator id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Iterable<Flight> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public boolean containsOfIdentity(final FlightDesignator id) {
            return byId.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final FlightDesignator entityId) {
            byId.remove(entityId);
        }

        @Override
        public void delete(final Flight entity) {
            deleteOfIdentity(entity.identity());
        }

        @Override
        public long count() {
            return byId.size();
        }
    }

    static final class InMemoryAircraftModelRepository
            extends InMemoryDomainRepository<AircraftModel, AircraftModelId>
            implements AircraftModelRepository {

        @Override
        public Optional<AircraftModel> findByID(final AircraftModelId id) {
            return ofIdentity(id);
        }

        @Override
        public Optional<AircraftModel> existsByNameAndManufacturer(final ModelName name,
                                                                   final ManufacturerId manufacturerId) {
            return Optional.empty();
        }

        @Override
        public Iterable<AircraftModel> findByManufacturer(final ManufacturerId manufacturerId) {
            return java.util.List.of();
        }
    }

    static final class InMemoryCompanyCollaboratorUserRepository
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

    @Override
    public TransactionalContext newTransactionalContext() {
        return null;
    }

    @Override
    public UserRepository users(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public UserRepository users() {
        return null;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return null;
    }

    @Override
    public AirControlAreaRepository airControlArea(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return null;
    }

    @Override
    public WeatherDataRepository weatherData(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public WeatherDataRepository weatherData() {
        return null;
    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirportRepository airports() {
        return null;
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators(final TransactionalContext autoTx) {
        return collaborators;
    }

    @Override
    public ManufacturerRepository manufacturers(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return null;
    }

    @Override
    public EngineModelRepository engineModels(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public EngineModelRepository engineModels() {
        return null;
    }

    @Override
    public AircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return aircraftModels;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
        return null;
    }

    @Override
    public AircraftRepository aircraft(final TransactionalContext autoTx) {
        return aircraft;
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return flights;
    }

    @Override
    public RouteRepository routes(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public RouteRepository routes() {
        return null;
    }

    @Override
    public EmailDomainRepository emailDomains(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public EmailDomainRepository emailDomains() {
        return null;
    }
}
