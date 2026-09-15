package eapli.aisafe.manufacturermanagement.application;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.repositories.ManufacturerRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class ListManufacturersController {

    private final AuthorizationService authz;
    private final ManufacturerRepository repo;

    public ListManufacturersController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().manufacturers());
    }

    public ListManufacturersController(final AuthorizationService authz,
                                       final ManufacturerRepository repo) {
        if (authz == null || repo == null) throw new IllegalArgumentException();
        this.authz = authz;
        this.repo = repo;
    }

    public Iterable<Manufacturer> allManufacturers() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR);
        return repo.findAll();
    }
}

