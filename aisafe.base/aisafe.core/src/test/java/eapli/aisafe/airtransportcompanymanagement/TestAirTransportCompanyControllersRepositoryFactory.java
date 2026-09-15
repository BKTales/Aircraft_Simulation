package eapli.aisafe.airtransportcompanymanagement;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal {@link RepositoryFactory} for tests covering no-arg air-transport-company controllers.
 */
public class TestAirTransportCompanyControllersRepositoryFactory implements RepositoryFactory {

    private final AirTransportCompanyRepository companies = new InMemoryAirTransportCompanyRepository();

    private static final class InMemoryAirTransportCompanyRepository implements AirTransportCompanyRepository {
        private final Map<IATACode, AirTransportCompany> byIata = new HashMap<>();
        private final Map<ICAOCode, AirTransportCompany> byIcao = new HashMap<>();

        @Override
        public Optional<AirTransportCompany> findByIataCode(final IATACode code) {
            return Optional.ofNullable(byIata.get(code));
        }

        @Override
        public Optional<AirTransportCompany> findByIcaoCode(final ICAOCode code) {
            return Optional.ofNullable(byIcao.get(code));
        }

        @Override
        public AirTransportCompany save(final AirTransportCompany entity) {
            byIata.put(entity.identity(), entity);
            byIcao.put(entity.icaoCode(), entity);
            return entity;
        }

        @Override
        public Iterable<AirTransportCompany> findAll() {
            return byIata.values();
        }

        @Override
        public Optional<AirTransportCompany> ofIdentity(final IATACode id) {
            return findByIataCode(id);
        }

        @Override
        public boolean containsOfIdentity(final IATACode id) {
            return byIata.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final IATACode entityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(final AirTransportCompany entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long count() {
            return byIata.size();
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
        return companies;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return companies;
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
        return null;
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return null;
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
    public FlightControlOperatorUserRepository flightOperators(final TransactionalContext autoTx) {
        return null;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
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
