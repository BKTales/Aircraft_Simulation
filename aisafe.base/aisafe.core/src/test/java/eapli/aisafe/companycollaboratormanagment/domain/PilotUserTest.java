package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PilotUserTest {

    @Test
    void deactivateFromRosterSetsSystemUserInactive() {
        final PilotUser pilot = samplePilot("pilot@tap.com");

        pilot.deactivateFromRoster(Calendar.getInstance());

        assertFalse(pilot.systemUser().isActive());
    }

    @Test
    void deactivateFromRosterThrowsWhenAlreadyInactive() {
        final PilotUser pilot = samplePilot("inactive@tap.com");
        pilot.systemUser().deactivate(Calendar.getInstance());

        assertThrows(IllegalStateException.class,
                () -> pilot.deactivateFromRoster(Calendar.getInstance()));
    }


    @Test
    void constructorRejectsNullCompany() {
        final SystemUser user = buildUser("a@tap.com");
        final SecurityClearance clearance = SecurityClearance.valueOf(LocalDate.of(2027, 1, 1));
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.of(2024, 1, 1));
        final Phone phone = Phone.valueOf("+351999999999");

        assertThrows(IllegalArgumentException.class,
                () -> new PilotUser(user, clearance, null, phone, skillsAssessment));
    }


    @Test
    void toDTOMapsEmail() {
        final PilotUser pilot = samplePilot("dto@tap.com");

        assertEquals("dto@tap.com", pilot.toDTO().getEmail());
    }

    @Test
    void toDTOMapsFirstName() {
        final PilotUser pilot = samplePilot("dto@tap.com");

        assertEquals("John", pilot.toDTO().getFirstName());
    }

    @Test
    void toDTOMapsLastName() {
        final PilotUser pilot = samplePilot("dto@tap.com");

        assertEquals("Doe", pilot.toDTO().getLastName());
    }

    @Test
    void toDTOMapsPhoneNumber() {
        final PilotUser pilot = samplePilot("dto@tap.com");

        assertEquals("+351999999999", pilot.toDTO().getPhoneNumber());
    }

    @Test
    void toDTOCountsCertifications() {
        final PilotUser pilot = samplePilot("dto@tap.com");
        final DueDate due = DueDate.valueOf(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1));
        pilot.addPilotCertification(new PilotCertification(mock(AircraftModel.class), due));

        assertEquals(1, pilot.toDTO().getCertificationCount());
    }

    @Test
    void addPilotCertificationIncreasesListSize() {
        final PilotUser pilot = samplePilot("cert@tap.com");
        final DueDate due = DueDate.valueOf(LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1));

        pilot.addPilotCertification(new PilotCertification(mock(AircraftModel.class), due));

        assertEquals(1, pilot.pilotCertifications().size());
    }

    @Test
    void addPilotCertificationContainsCertification() {
        final PilotUser pilot = samplePilot("cert@tap.com");
        final DueDate due = DueDate.valueOf(LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1));
        final PilotCertification certification = new PilotCertification(mock(AircraftModel.class), due);

        pilot.addPilotCertification(certification);

        assertTrue(pilot.pilotCertifications().contains(certification));
    }

    @Test
    void sameAsReturnsTrueForSameInstance() {
        final PilotUser pilot = samplePilot("same@tap.com");

        assertTrue(pilot.sameAs(pilot));
    }

    @Test
    void sameAsReturnsFalseForDifferentPilot() {
        final PilotUser a = samplePilot("same@tap.com");
        final PilotUser b = samplePilot("other@tap.com");

        assertFalse(a.sameAs(b));
    }

    @Test
    void sameAsReturnsFalseForNull() {
        final PilotUser pilot = samplePilot("same@tap.com");

        assertFalse(pilot.sameAs(null));
    }

    @Test
    void sameAsReturnsFalseForDifferentType() {
        final PilotUser pilot = samplePilot("same@tap.com");

        assertFalse(pilot.sameAs("not-a-pilot"));
    }


    @Test
    void identityIsNotNull() {
        assertNotNull(samplePilot("getters@tap.com").identity());
    }

    @Test
    void airTransportCompanyGetterReturnsCorrectCompany() {
        final AirTransportCompany company = tapCompany();

        assertEquals(company, samplePilot("getters@tap.com", company).airTransportCompany());
    }

    @Test
    void systemUserEmailMatchesConstructorArgument() {
        assertEquals("getters@tap.com",
                samplePilot("getters@tap.com").systemUser().email().toString());
    }


    @Test
    void protectedConstructorForOrmDoesNotThrow() {
        assertNotNull(new PilotUser());
    }

    private static PilotUser samplePilot(final String email) {
        return samplePilot(email, tapCompany());
    }

    private static PilotUser samplePilot(final String email, final AirTransportCompany company) {
        return new PilotUser(
                buildUser(email),
                SecurityClearance.valueOf(LocalDate.of(2027, 1, 1)),
                company,
                Phone.valueOf("+351999999999"),
                SkillsAssessment.valueOf(LocalDate.of(2024,1,1)));
    }

    private static AirTransportCompany tapCompany() {
        return new AirTransportCompany(
                CompanyName.valueOf("TAP"), IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
    }

    private static SystemUser buildUser(final String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "John", "Doe", email)
                .build();
    }
}
