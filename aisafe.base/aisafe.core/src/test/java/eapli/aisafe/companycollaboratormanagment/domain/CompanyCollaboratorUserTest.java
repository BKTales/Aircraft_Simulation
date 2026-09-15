package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyCollaboratorUserTest {

    @Test
    void ensureThatConstructorRequiresAllFields() {
        final SystemUser user = buildUser("a@tap.com");
        final SecurityClearance clearance = SecurityClearance.valueOf(LocalDate.of(2027, 4, 25));
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.of(2026, 4, 25));

        final AirTransportCompany company = new AirTransportCompany(CompanyName.valueOf("TAP"),
                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
        final Phone phone = Phone.valueOf("+351999999999");

        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCollaboratorUser(null, clearance, company, phone,skillsAssessment));


        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCollaboratorUser(user, null, company, phone,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCollaboratorUser(user, clearance, null, phone,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCollaboratorUser(user, clearance, company, null,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCollaboratorUser(user, clearance, company, phone,null));
    }

    @Test
    void ensureThatIsActiveReflectsSystemUser() {
        final CompanyCollaboratorUser activeUser =
                new CompanyCollaboratorUser(
                        buildUser("a@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        final CompanyCollaboratorUser inactiveUser =
                new CompanyCollaboratorUser(
                        buildUser("b@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        inactiveUser.systemUser().deactivate(Calendar.getInstance());
        assertTrue(activeUser.systemUser().isActive());
        assertFalse(inactiveUser.systemUser().isActive());
    }

    @Test
    void ensureThatDeactivateChangesStatus() {
        final SystemUser user = buildUser("a@tap.com");

        final CompanyCollaboratorUser subject =
                new CompanyCollaboratorUser(user,
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        user.deactivate(Calendar.getInstance());

        assertFalse(subject.systemUser().isActive());
    }

    @Test
    void ensureThatIdentityIsNotNull() {
        final CompanyCollaboratorUser subject =
                new CompanyCollaboratorUser(
                        buildUser("a@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        assertNotNull(subject.identity());
    }

    @Test
    void ensureThatGettersExposeValues() {
        final SystemUser user = buildUser("a@tap.com");
        final SecurityClearance clearance = SecurityClearance.valueOf(LocalDate.of(2027, 4, 25));
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.of(2026, 4, 25));
        final AirTransportCompany company = new AirTransportCompany(CompanyName.valueOf("TAP"),
                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
        final Phone phone = Phone.valueOf("+351999999999");

        final CompanyCollaboratorUser subject =
                new CompanyCollaboratorUser(user, clearance, company, phone,skillsAssessment);

        assertEquals(company, subject.airTransportCompany());
        assertEquals(user, subject.systemUser());
        assertEquals(clearance, subject.securityClearance());
    }

    @Test
    void ensureThatSameAsUsesIdentity() {
        final SystemUser user = buildUser("a@tap.com");

        final CompanyCollaboratorUser a =
                new CompanyCollaboratorUser(user,
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        final CompanyCollaboratorUser b =
                new CompanyCollaboratorUser(user,
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        final CompanyCollaboratorUser c =
                new CompanyCollaboratorUser(
                        buildUser("different@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2027, 4, 25)),
                        new AirTransportCompany(CompanyName.valueOf("TAP"),
                                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")),
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        assertTrue(a.sameAs(a));
        assertFalse(a.sameAs(b));
        assertFalse(a.sameAs(c));
        assertTrue(a.systemUser().sameAs(b.systemUser()));
        assertFalse(a.systemUser().sameAs(c.systemUser()));
        assertFalse(a.sameAs(null));
        assertFalse(a.sameAs("string"));
    }


    @Test
    void ensureProtectedConstructorForORM() {
        final CompanyCollaboratorUser instance = new CompanyCollaboratorUser();

        assertNotNull(instance);
    }

    // helper
    private SystemUser buildUser(String email) {
        final SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "John", "Doe", email)
                .build();


        return user;
    }
}
