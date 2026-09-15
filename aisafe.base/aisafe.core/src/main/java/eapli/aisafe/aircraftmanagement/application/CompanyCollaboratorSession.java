package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Username;

import java.util.Optional;

final class CompanyCollaboratorSession {

    private CompanyCollaboratorSession() {
    }

    static CompanyCollaboratorUser requireAirTransportCompanyCollaborator(
            final AuthorizationService authz,
            final CompanyCollaboratorUserRepository collaborators) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final Optional<UserSession> session = authz.session();
        if (session.isEmpty()) {
            throw new IllegalStateException("No authenticated user in session.");
        }
        final Object principal;
        try {
            principal = session.get().authenticatedUser().identity();
        } catch (final ClassCastException ex) {
            throw new IllegalStateException("Unexpected authenticated principal.", ex);
        }
        if (!(principal instanceof Username username)) {
            throw new IllegalStateException("Unexpected authenticated principal.");
        }

        final Optional<CompanyCollaboratorUser> collaborator = collaborators.findByUsername(username);
        if (collaborator.isEmpty()) {
            throw new IllegalStateException("Only registered air transport company collaborators may manage aircraft.");
        }
        return collaborator.get();
    }
}
