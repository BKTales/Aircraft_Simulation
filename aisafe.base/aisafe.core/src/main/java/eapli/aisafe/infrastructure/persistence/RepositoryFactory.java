package eapli.aisafe.infrastructure.persistence;

import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

public interface RepositoryFactory {

    TransactionalContext newTransactionalContext();

    UserRepository users(TransactionalContext autoTx);

    UserRepository users();

    AirTransportCompanyRepository airTransportCompanies(TransactionalContext autoTx);

    AirTransportCompanyRepository airTransportCompanies();

    AirControlAreaRepository airControlArea(TransactionalContext autoTx);

    AirControlAreaRepository airControlArea();

    WeatherDataRepository weatherData(TransactionalContext autoTx);

    WeatherDataRepository weatherData();

    AirportRepository airports(TransactionalContext autoTx);

    AirportRepository airports();

    CompanyCollaboratorUserRepository collaborators(TransactionalContext autoTx);

    CompanyCollaboratorUserRepository collaborators();

    PilotUserRepository pilots(TransactionalContext autoTx);

    PilotUserRepository pilots();

    ManufacturerRepository manufacturers(TransactionalContext autoTx);

    ManufacturerRepository manufacturers();

    EngineModelRepository engineModels(TransactionalContext autoTx);

    EngineModelRepository engineModels();

    AircraftModelRepository aircraftModels(TransactionalContext autoTx);

    AircraftModelRepository aircraftModels();

    AircraftRepository aircraft(TransactionalContext autoTx);

    AircraftRepository aircraft();

    FlightRepository flights(TransactionalContext autoTx);

    FlightRepository flights();

    RouteRepository routes(TransactionalContext autoTx);

    RouteRepository routes();

    FlightControlOperatorUserRepository flightOperators(TransactionalContext autoTx);

    FlightControlOperatorUserRepository flightOperators();

    EmailDomainRepository emailDomains(TransactionalContext autoTx);

    EmailDomainRepository emailDomains();
}
