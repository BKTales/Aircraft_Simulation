package eapli.aisafe.infrastructure.bootstrapers;

import java.util.HashSet;
import java.util.Set;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;

public class MasterUsersBootstrapper extends AbstractUserBootstrapper implements Action {
    @Override
    public boolean execute() {
        registerBackofficeOperator("backoffice", TestDataConstants.PASSWORD1,
                "Bob", "Backoffice", "backoffice@aisafe.backoffice.com");
        registerWeatherPerson("weather", TestDataConstants.PASSWORD1, "Weather", "Report", "weather@aisafe.weather.com");
        return true;
    }

    private void registerBackofficeOperator(final String username, final String password, final String firstName,
                                            final String lastName, final String email) {
        final Set<Role> roles = new HashSet<>();
        roles.add(AISafeRoles.BACKOFFICE_OPERATOR);
        registerUser(username, password, firstName, lastName, email, roles);
    }

    private void registerWeatherPerson(final String username, final String password, final String firstName,
                                            final String lastName, final String email) {
        final Set<Role> roles = new HashSet<>();
        roles.add(AISafeRoles.WEATHER_PERSON);
        registerUser(username, password, firstName, lastName, email, roles);
    }
}
