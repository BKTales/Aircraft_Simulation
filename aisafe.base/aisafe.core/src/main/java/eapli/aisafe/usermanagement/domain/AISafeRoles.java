package eapli.aisafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Role;

/**
 * Defines all system roles recognised by the AISafe application.
 *
 * @author Paulo Gandra Sousa
 */
public final class AISafeRoles {

    public static final Role POWER_USER = Role.valueOf("POWER_USER");
    public static final Role ADMIN = Role.valueOf("ADMIN");
    public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");
    public static final Role WEATHER_PERSON = Role.valueOf("WEATHER_PERSON");
    public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");
    public static final Role PILOT = Role.valueOf("PILOT");
    public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");

    private AISafeRoles() {
    }

    /**
     * Returns all roles that represent real system actors (i.e. excluding the
     * internal bootstrap-only {@code POWER_USER}).
     *
     * @return array of non-power-user roles
     */
    public static Role[] nonUserValues() {
        return new Role[] {
            ADMIN,
            BACKOFFICE_OPERATOR,
            WEATHER_PERSON,
            AIR_TRANSPORT_COMPANY_COLLABORATOR,
            PILOT,
            FLIGHT_CONTROL_OPERATOR
        };
    }

    /**
     * Returns roles that may be assigned when registering a user via US031
     * (backoffice user management by an administrator).
     *
     * @return array of backoffice-registerable roles
     */
    public static Role[] backofficeRegisterableValues() {
        return new Role[] {
            ADMIN,
            BACKOFFICE_OPERATOR,
            WEATHER_PERSON
        };
    }

    public static boolean isBackofficeRegisterable(final Role role) {
        if (role == null) {
            return false;
        }
        for (final Role allowed : backofficeRegisterableValues()) {
            if (role.equals(allowed)) {
                return true;
            }
        }
        return false;
    }
}
