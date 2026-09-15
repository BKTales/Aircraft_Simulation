package eapli.aisafe.airportmanagement;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
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
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

import java.util.List;

/**
 * Minimal {@link RepositoryFactory} for tests that need {@link PersistenceContext#repositories()}
 * when covering no-arg controller constructors.
 */
public class TestAirportControllersRepositoryFactory implements RepositoryFactory {

    private final AirportRepository airports = new InMemoryAirportRepository();
    private final AirControlAreaRepository airControlArea = new InMemoryAirControlAreaRepository();

    public TestAirportControllersRepositoryFactory() {
        final AirControlArea area = new AirControlArea(
                new AirControlAreaName("Test Area"),
                new GeographicBoundary(List.of(
                        new GeographicCoords(38.0f, -9.0f),
                        new GeographicCoords(39.0f, -9.0f),
                        new GeographicCoords(39.0f, -8.0f),
                        new GeographicCoords(38.0f, -8.0f))),
                new MinFuelRequirement(250.0f));
        airControlArea.save(area);
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
        return airControlArea;
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return airControlArea;
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
        return airports;
    }

    @Override
    public AirportRepository airports() {
        return airports;
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
