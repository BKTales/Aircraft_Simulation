package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Use-case controller for registering a new airport (US052).
 *
 * <p>Only users with the {@code BACKOFFICE_OPERATOR} role may invoke this use case.
 * The air control area is resolved automatically from the airport's coordinates.</p>
 *
 * @author aisafe team
 */
@UseCaseController
public class CreateAirportController {

    private final AuthorizationService authz;
    private final AirportService service;

    public CreateAirportController() {
        this(AuthzRegistry.authorizationService(),
                new AirportService(
                        PersistenceContext.repositories().airports(),
                        PersistenceContext.repositories().airControlArea()));
    }

    /**
     * Injectable constructor for testing.
     *
     * @param authz   the authorisation service
     * @param service the airport domain service
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public CreateAirportController(final AuthorizationService authz, final AirportService service) {
        if(authz == null || service == null){
            throw new IllegalArgumentException("Authorization service and airport service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    /**
     * Registers a new airport. The air control area is determined automatically
     * from the provided coordinates.
     *
     * @param iataCode        the airport's IATA code
     * @param icaoCode        the airport's ICAO code
     * @param latitude        latitude in decimal degrees
     * @param longitude       longitude in decimal degrees
     * @param elevationMeters elevation in metres above sea level
     * @return the newly created {@link Airport}
     * @throws NoAreaFoundForCoordinatesException if no area contains the given coordinates
     */
    public Airport createAirport(final String iataCode, final String icaoCode,
                                 final double latitude, final double longitude,
                                 final double elevationMeters) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        return service.registerAirport(iataCode, icaoCode, latitude, longitude, elevationMeters);
    }
}
