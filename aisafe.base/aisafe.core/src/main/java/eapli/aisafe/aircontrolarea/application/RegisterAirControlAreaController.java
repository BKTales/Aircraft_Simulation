package eapli.aisafe.aircontrolarea.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.airtransportcompanymanagement.application.AirTransportCompanyService;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

public class RegisterAirControlAreaController {

    private final AuthorizationService authz;
    private final AirControlAreaService service;

    public RegisterAirControlAreaController() {
        this(AuthzRegistry.authorizationService(),
                new AirControlAreaService(PersistenceContext.repositories().airControlArea()));
    }

    public Iterable<AirControlArea> availableAreas() {
        return service.availableAreas();
    }

    public RegisterAirControlAreaController(final AuthorizationService authz,
                                                 final AirControlAreaService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and company service are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public AirControlArea registerAirControlArea(final String name,
                                                 final List<float[]> rawCoords,
                                                 final float minFuel) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);

        return service.registerNewArea(name, rawCoords, minFuel);
    }

}
