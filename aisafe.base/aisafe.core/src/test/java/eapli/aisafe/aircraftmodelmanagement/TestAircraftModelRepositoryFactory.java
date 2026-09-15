package eapli.aisafe.aircraftmodelmanagement;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Test-only {@link RepositoryFactory} used to cover default constructors that rely on
 * {@code PersistenceContext.repositories()} via reflection-based factory loading.
 *
 * <p>This factory provides in-memory repositories for manufacturers and engine models so
 * controllers/services can be instantiated in unit tests without JPA.</p>
 */
public class TestAircraftModelRepositoryFactory implements RepositoryFactory {

    private final AircraftModelRepository aircraftModelRepository = new DummyAircraftModelRepository();
    private final ManufacturerRepository manufacturers = new DummyManufacturerRepository();
    private final EngineModelRepository engineModels = new DummyEngineModelRepository();

    public AircraftModelRepository aircraftModelRepository() {
        return aircraftModelRepository;
    }

    private static class DummyManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
            implements ManufacturerRepository {
        @Override
        public Optional<Manufacturer> findById(final ManufacturerId id) {
            return Optional.ofNullable(data().get(id));
        }
    }

    private static class DummyEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
            implements EngineModelRepository {
        @Override
        public Optional<EngineModel> findByModelId(final EngineModelId id) {
            return Optional.ofNullable(data().get(id));
        }

        @Override
        public Optional<EngineModel> findByNameAndManufacturer(final EngineName name, final ManufacturerId manufacturerId) {
            return Optional.empty();
        }
    }

    private static class DummyAircraftModelRepository
            extends InMemoryDomainRepository<AircraftModel, AircraftModelId>
            implements AircraftModelRepository {

        @Override
        public Optional<AircraftModel> findByID(final AircraftModelId id) {
            return data().values().stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.identity().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<AircraftModel> existsByNameAndManufacturer(final ModelName name,
                                                                   final ManufacturerId manufacturerId) {

            return data().values().stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.name().equals(name))
                    .filter(e -> e.manufacturer().equals(manufacturerId))
                    .findFirst();
        }

        @Override
        public Iterable<AircraftModel> findByManufacturer(final ManufacturerId manufacturerId) {

            return data().values().stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.manufacturer().equals(manufacturerId))
                    .toList();
        }
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return null;
    }

    @Override
    public UserRepository users(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public UserRepository users() {
        return null;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return null;
    }

    @Override
    public AirControlAreaRepository airControlArea(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return null;
    }

    @Override
    public WeatherDataRepository weatherData(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public WeatherDataRepository weatherData() {
        return null;
    }

    @Override
    public AirportRepository airports(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AirportRepository airports() {
        return null;
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators() {
        return null;
    }

    @Override
    public PilotUserRepository pilots(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public PilotUserRepository pilots() {
        return null;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
        return null;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public ManufacturerRepository manufacturers(TransactionalContext autoTx) {
        return manufacturers;
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return manufacturers;
    }

    @Override
    public EngineModelRepository engineModels(TransactionalContext autoTx) {
        return engineModels;
    }

    @Override
    public EngineModelRepository engineModels() {
        return engineModels;
    }

    @Override
    public AircraftModelRepository aircraftModels(TransactionalContext autoTx) {
        return aircraftModelRepository;
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return aircraftModels(null);
    }

    @Override
    public AircraftRepository aircraft(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public AircraftRepository aircraft() {
        return null;
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public FlightRepository flights() {
        return null;
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
    public EmailDomainRepository emailDomains(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public EmailDomainRepository emailDomains() {
        return null;
    }
}

