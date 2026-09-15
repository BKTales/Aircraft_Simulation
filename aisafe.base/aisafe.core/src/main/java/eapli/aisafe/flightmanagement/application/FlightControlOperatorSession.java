package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Username;

import java.util.Optional;

final class FlightControlOperatorSession {

    private FlightControlOperatorSession() {
    }

    static FlightControlOperatorUser requireFlightControlOperator(
            final AuthorizationService authz,
            final FlightControlOperatorUserRepository operators) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);

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

        final Optional<FlightControlOperatorUser> operator = operators.findByUsername(username);
        if (operator.isEmpty()) {
            throw new IllegalStateException("Only registered flight control operators may access this feature.");
        }
        return operator.get();
    }
}
