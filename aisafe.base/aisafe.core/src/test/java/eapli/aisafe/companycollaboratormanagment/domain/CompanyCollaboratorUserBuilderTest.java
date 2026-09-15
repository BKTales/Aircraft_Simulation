package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyCollaboratorUserBuilderTest {

    private SystemUser buildSystemUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.com", "Password1", "Pilot", "Tap", "pilot@tap.com")
                .build();
    }

    private AirTransportCompany buildCompany() {
        return new AirTransportCompany(CompanyName.valueOf("TAP"),
                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
    }

    @Test
    void ensureThatBuildCreatesCollaboratorFromSystemUserData() {
        final CompanyCollaboratorUserBuilder builder = new CompanyCollaboratorUserBuilder()
                .withSystemUser(buildSystemUser())
                .withAirTransportCompany(buildCompany())
                .withSecurityData("2027-04-25")
                .withPhoneNumber("+351999999999")
                .withSkillsData("2026-01-01");

        final CompanyCollaboratorUser subject = builder.build();

        assertEquals("Pilot", subject.systemUser().name().firstName());
        assertEquals("Tap", subject.systemUser().name().lastName());
        assertTrue(subject.systemUser().isActive());
    }

    @Test
    void ensureThatMissingSystemUserThrows() {
        final CompanyCollaboratorUserBuilder builder = new CompanyCollaboratorUserBuilder()
                .withAirTransportCompany(buildCompany())
                .withSecurityData("2027-04-25");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void ensureThatAllWithMethodsReturnSameBuilder() {
        final CompanyCollaboratorUserBuilder builder = new CompanyCollaboratorUserBuilder();

        assertNotNull(builder.withSystemUser(buildSystemUser()));
        assertNotNull(builder.withAirTransportCompany(buildCompany()));
        assertNotNull(builder.withSecurityData("2026-04-25"));
    }
}
