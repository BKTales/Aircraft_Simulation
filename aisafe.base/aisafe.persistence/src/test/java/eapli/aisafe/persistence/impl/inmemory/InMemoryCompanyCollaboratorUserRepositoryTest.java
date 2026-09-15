package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.domain.SkillsAssessment;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCompanyCollaboratorUserRepositoryTest {

    @Test
    void findByUsernameReturnsCollaboratorWhenPresent() {
        final var repo = new InMemoryCompanyCollaboratorUserRepository();
        final AirTransportCompany company = new AirTransportCompany(CompanyName.valueOf("TAP"),
                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
        final SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.com", "password", "John", "Doe", "pilot@tap.com")
                .build();
        final CompanyCollaboratorUser collaborator = new CompanyCollaboratorUser(
                user,
                SecurityClearance.valueOf(LocalDate.of(2027, 1, 1)),
                company,
                Phone.valueOf("+351999999999"),
                SkillsAssessment.valueOf(LocalDate.of(2026,1,2))
        );
        repo.save(collaborator);

        assertTrue(repo.findByUsername(Username.valueOf("pilot@tap.com")).isPresent());
    }

    @Test
    void findATCCByCompanyAndActiveReturnsOnlyActiveCollaborators() {
        final var repo = new InMemoryCompanyCollaboratorUserRepository();
        final AirTransportCompany company = new AirTransportCompany(CompanyName.valueOf("TAP"),
                IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));

        final SystemUser activeUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("active@tap.com", "password", "John", "Doe", "active@tap.com")
                .build();
        final CompanyCollaboratorUser active = new CompanyCollaboratorUser(
                activeUser,
                SecurityClearance.valueOf(LocalDate.of(2027, 1, 1)),
                company,
                Phone.valueOf("+351999999999"),
                SkillsAssessment.valueOf(LocalDate.of(2026,1,2))
        );
        repo.save(active);

        final SystemUser inactiveUser = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("inactive@tap.com", "password", "Jane", "Doe", "inactive@tap.com")
                .build();
        inactiveUser.deactivate(Calendar.getInstance());
        final CompanyCollaboratorUser inactive = new CompanyCollaboratorUser(
                inactiveUser,
                SecurityClearance.valueOf(LocalDate.of(2027, 1, 1)),
                company,
                Phone.valueOf("+351999999999"),
                SkillsAssessment.valueOf(LocalDate.of(2026,1,2))
        );
        repo.save(inactive);

        final var result = repo.findATCCByCompanyAndActive(company);
        assertEquals(1, StreamSupport.stream(result.spliterator(), false).count());
    }

}
