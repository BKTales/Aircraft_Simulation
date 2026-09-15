package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.aircontrolarea.repositories.InMemoryAirControlAreaRepository;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.persistence.impl.inmemory.InMemoryAircraftModelRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.InMemoryWeatherDataRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;

public class InMemoryRepositoryFactory implements RepositoryFactory {
    private static final InMemoryUserRepository USER_REPO = new InMemoryUserRepository();
    private static final InMemoryAirTransportCompanyRepository AIR_COMPANY_REPO = new InMemoryAirTransportCompanyRepository();
    private static final InMemoryAirControlAreaRepository AIR_CONTROL_REPO = new InMemoryAirControlAreaRepository();
    private static final InMemoryAirportRepository AIRPORT_REPO = new InMemoryAirportRepository();
    private static final InMemoryCompanyCollaboratorUserRepository COMPANY_COLLABORATOR_USERS = new InMemoryCompanyCollaboratorUserRepository();
    private static final InMemoryPilotUserRepository PILOTS = new InMemoryPilotUserRepository();
    private static final InMemoryFlightControlOperatorUserRepository FLIGHT_CONTROL_OPERATOR_USERS = new InMemoryFlightControlOperatorUserRepository();
    private static final InMemoryWeatherDataRepository WEATHER_DATA_REPOSITORY = new InMemoryWeatherDataRepository();
    private static final InMemoryManufacturerRepository MANUFACTURERS = new InMemoryManufacturerRepository();
    private static final InMemoryEngineModelRepository ENGINE_MODELS = new InMemoryEngineModelRepository();
    private static final InMemoryAircraftModelRepository AIRCRAFT_MODELS = new InMemoryAircraftModelRepository();
    private static final InMemoryAircraftRepository AIRCRAFT = new InMemoryAircraftRepository();
    private static final InMemoryFlightRepository FLIGHTS = new InMemoryFlightRepository();
    private static final InMemoryRouteRepository ROUTES = new InMemoryRouteRepository();
    private static final InMemoryEmailDomainRepository EMAIL_DOMAINS = new InMemoryEmailDomainRepository();
    private static boolean seeded;

    @Override
    public UserRepository users(final TransactionalContext autoTx) {
        if(!seeded){
            final var userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
            userBuilder.withUsername("poweruser")
                    .withPassword("poweruserA1")
                    .withName("Power", "User")
                    .withEmail("poweruser@aisafe.local")
                    .withRoles(AISafeRoles.POWER_USER);
            USER_REPO.save(userBuilder.build());
            seeded = true;
        }
        return USER_REPO;
    }

    @Override
    public UserRepository users() {
        return users(null);
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies(final TransactionalContext autoTx) {
        return AIR_COMPANY_REPO;
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return airTransportCompanies(null);
    }

    @Override
    public AirControlAreaRepository airControlArea(TransactionalContext autoTx) {
        return AIR_CONTROL_REPO;
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return airControlArea(null);
    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return AIRPORT_REPO;
    }

    @Override
    public AirportRepository airports() {
        return airports(null);
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators(final TransactionalContext autoTx) {
        return COMPANY_COLLABORATOR_USERS;
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
        return flightOperators(null);
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators(final TransactionalContext autoTx) {
        return FLIGHT_CONTROL_OPERATOR_USERS;
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators() {
        return collaborators(null);
    }

    @Override
    public PilotUserRepository pilots(TransactionalContext autoTx) {
        return PILOTS;
    }

    @Override
    public PilotUserRepository pilots() {
        return pilots(null);
    }

    @Override
    public WeatherDataRepository weatherData(TransactionalContext autoTx){
        return WEATHER_DATA_REPOSITORY;
    }

    @Override
    public WeatherDataRepository weatherData() {
        return weatherData(null);
    }

    @Override
    public ManufacturerRepository manufacturers(final TransactionalContext autoTx) {
        return MANUFACTURERS;
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return manufacturers(null);
    }

    @Override
    public EngineModelRepository engineModels(final TransactionalContext autoTx) {
        return ENGINE_MODELS;
    }

    @Override
    public EngineModelRepository engineModels() {
        return engineModels(null);
    }

    @Override
    public AircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return AIRCRAFT_MODELS;
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return aircraftModels(null);
    }

    @Override
    public AircraftRepository aircraft(final TransactionalContext autoTx) {
        return AIRCRAFT;
    }

    @Override
    public AircraftRepository aircraft() {
        return aircraft(null);
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return FLIGHTS;
    }

    @Override
    public FlightRepository flights() {
        return flights(null);
    }

    @Override
    public RouteRepository routes(final TransactionalContext autoTx) {
        return ROUTES;
    }

    @Override
    public RouteRepository routes() {
        return routes(null);
    }

    @Override
    public EmailDomainRepository emailDomains(final TransactionalContext autoTx) {
        return EMAIL_DOMAINS;
    }

    @Override
    public EmailDomainRepository emailDomains() {
        return emailDomains(null);
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return null;
    }
}
