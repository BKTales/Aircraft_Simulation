package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.aircontrolarea.repositories.JpaAirControlAreaRepository;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.infrastructure.persistence.RepositoryFactory;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.JpaWeatherDataRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaRepositoryFactory implements RepositoryFactory {

    @Override
    public UserRepository users(final TransactionalContext autoTx) {
        return new JpaAutoTxUserRepository(autoTx);
    }

    @Override
    public UserRepository users() {
        return new JpaAutoTxUserRepository(
                Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties());
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies(final TransactionalContext autoTx) {
        return new JpaAirTransportCompanyRepository(autoTx);
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return new JpaAirTransportCompanyRepository(Application.settings().getPersistenceUnitName());
    }


    @Override
    public AirControlAreaRepository airControlArea(final TransactionalContext autoTx) {
        return new JpaAirControlAreaRepository(autoTx);
    }

    @Override
    public AirControlAreaRepository airControlArea() {
        return new JpaAirControlAreaRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public WeatherDataRepository weatherData(TransactionalContext autoTx) {
        return new JpaWeatherDataRepository(autoTx);
    }

    @Override
    public WeatherDataRepository weatherData() {
        return new JpaWeatherDataRepository(Application.settings().getPersistenceUnitName());

    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return new JpaAirportRepository(autoTx);
    }

    @Override
    public AirportRepository airports() {
        return new JpaAirportRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators(TransactionalContext autoTx) {
        return new JpaCompanyCollaboratorUserRepository(autoTx);
    }

    @Override
    public CompanyCollaboratorUserRepository collaborators() {
        return new JpaCompanyCollaboratorUserRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public PilotUserRepository pilots(TransactionalContext autoTx) {
        return new JpaPilotUserRepository(autoTx);
    }

    @Override
    public PilotUserRepository pilots() {
        return new JpaPilotUserRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators(TransactionalContext autoTx) {
        return new JpaFlightControlOperatorUserRepository(autoTx);
    }

    @Override
    public FlightControlOperatorUserRepository flightOperators() {
        return new JpaFlightControlOperatorUserRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public ManufacturerRepository manufacturers(final TransactionalContext autoTx) {
        return new JpaManufacturerRepository(autoTx);
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return new JpaManufacturerRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public EngineModelRepository engineModels(final TransactionalContext autoTx) {
        return new JpaEngineModelRepository(autoTx);
    }

    @Override
    public EngineModelRepository engineModels() {
        return new JpaEngineModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return new JpaAircraftModelRepository(autoTx);
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return new JpaAircraftModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AircraftRepository aircraft(final TransactionalContext autoTx) {
        return new JpaAircraftRepository(autoTx);
    }

    @Override
    public AircraftRepository aircraft() {
        return new JpaAircraftRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return new JpaFlightRepository(autoTx);
    }

    @Override
    public FlightRepository flights() {
        return new JpaFlightRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public RouteRepository routes(final TransactionalContext autoTx) {
        return new JpaRouteRepository(autoTx);
    }

    @Override
    public RouteRepository routes() {
        return new JpaRouteRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public EmailDomainRepository emailDomains(final TransactionalContext autoTx) {
        return new JpaEmailDomainRepository(autoTx);
    }

    @Override
    public EmailDomainRepository emailDomains() {
        return new JpaEmailDomainRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return JpaAutoTxRepository.buildTransactionalContext(
                Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties());
    }
}
