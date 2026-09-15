package eapli.aisafe.weatherdata;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircontrolarea.repositories.InMemoryAirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
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
import eapli.aisafe.weatherdata.repositories.InMemoryWeatherDataRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

public class TestWeatherDataRepositoryFactory implements RepositoryFactory {

    private final InMemoryAirControlAreaRepository airControlAreaRepository = new InMemoryAirControlAreaRepository();
    private final InMemoryWeatherDataRepository weatherDataRepository = new InMemoryWeatherDataRepository();
    private final InMemoryAirportRepository airportRepository = new InMemoryAirportRepository();

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
        return airControlAreaRepository;
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return airControlAreaRepository;
    }

    @Override
    public WeatherDataRepository weatherData(TransactionalContext autoTx) {
        return weatherDataRepository;
    }

    @Override
    public WeatherDataRepository weatherData() {
        return weatherDataRepository;
    }

    @Override
    public AirportRepository airports(TransactionalContext autoTx) {
        return airportRepository;
    }

    @Override
    public AirportRepository airports() {
        return airportRepository;
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
    public ManufacturerRepository manufacturers(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return null;
    }

    @Override
    public EngineModelRepository engineModels(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public EngineModelRepository engineModels() {
        return null;
    }

    @Override
    public AircraftModelRepository aircraftModels(TransactionalContext autoTx) {
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
    public FlightControlOperatorUserRepository flightOperators(TransactionalContext autoTx) {
        return null;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
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
