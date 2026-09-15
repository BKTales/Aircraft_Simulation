package eapli.aisafe.companycollaboratormanagment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompanyCollaboratorRolesTest {

    @Test
    void ensureThatNonUserValuesReturnsPilotAndATTC() {
        final var roles = CompanyCollaboratorRoles.nonUserValues();

        assertNotNull(CompanyCollaboratorRoles.ATCC);
        assertNotNull(CompanyCollaboratorRoles.PILOT);
        assertEquals(2, roles.length);
        assertEquals(CompanyCollaboratorRoles.ATCC, roles[0]);
        assertEquals(CompanyCollaboratorRoles.PILOT, roles[1]);
    }
}
