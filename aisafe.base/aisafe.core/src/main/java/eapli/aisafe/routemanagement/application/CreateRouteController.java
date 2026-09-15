package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@UseCaseController
public class CreateRouteController {

    private final AuthorizationService authz;
    private final RouteService service;
    private final CompanyCollaboratorUserRepository collaborators;

    public CreateRouteController() {
        this(AuthzRegistry.authorizationService(),
                new RouteService(
                        PersistenceContext.repositories().routes(),
                        PersistenceContext.repositories().airports()),
                PersistenceContext.repositories().collaborators());
    }

    public CreateRouteController(final AuthorizationService authz,
                                 final RouteService service,
                                 final CompanyCollaboratorUserRepository collaborators) {
        if (authz == null || service == null || collaborators == null) {
            throw new IllegalArgumentException("Authorization service, route service and collaborator repository are required.");
        }
        this.authz = authz;
        this.service = service;
        this.collaborators = collaborators;
    }

    public String currentCompanyContext() {
        return requireCompany().identity().toString();
    }

    public String validateRouteName(final String numericSuffix, final String companyIATACode) {
        final IATACode companyIataFromSession = requireCompany().identity();
        final IATACode companyIataFromRequest = IATACode.valueOf(companyIATACode);
        if (!companyIataFromSession.equals(companyIataFromRequest)) {
            throw new IllegalArgumentException("Invalid company context.");
        }
        return service.validateRouteName(companyIATACode + numericSuffix);
    }

    public Iterable<Airport> listAirports() {
        requireCompany();
        return service.listAirports();
    }

    public Route createCharterRoute(final String routeName,
                                    final String originAirportIata,
                                    final String destinationAirportIata,
                                    final LocalDate scheduledDeparture,
                                    final LocalDate scheduledArrival) {
        return service.createCharterRoute(
                routeName, originAirportIata, destinationAirportIata,
                requireCompany(), scheduledDeparture, scheduledArrival);
    }

    public Route createRegularRoute(final String routeName,
                                    final String originAirportIata,
                                    final String destinationAirportIata,
                                    final List<DayOfWeek> recurringDays) {
        return service.createRegularRoute(
                routeName, originAirportIata, destinationAirportIata, requireCompany(), recurringDays);
    }

    private AirTransportCompany requireCompany() {
        return CompanyRouteCollaboratorSession
                .requireAirTransportCompanyCollaborator(authz, collaborators)
                .airTransportCompany();
    }
}
