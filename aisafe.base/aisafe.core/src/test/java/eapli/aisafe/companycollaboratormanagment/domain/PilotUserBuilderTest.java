package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PilotUserBuilderTest {

    private static final DueDate VALID_DUE_DATE = DueDate.valueOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2026, 1, 1));

    private static PilotCertification someCertification() {
        return new PilotCertification(mock(AircraftModel.class), VALID_DUE_DATE);
    }

    private static PilotUserBuilder validBuilder() {
        return new PilotUserBuilder()
                .withSystemUser(buildUser("pilot@tap.com"))
                .withAirTransportCompany(tapCompany())
                .withSecurityData("2027-01-01")
                .withSkillAssessment("2024-01-01")
                .withPhoneNumber("+351999999999")
                .withCertification(someCertification());
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

    @Test
    void buildReturnsNonNullPilotUser() {
        assertNotNull(validBuilder().build());
    }

    @Test
    void buildSetsCorrectSystemUser() {
        final SystemUser user = buildUser("pilot@tap.com");
        final PilotUser result = validBuilder().withSystemUser(user).build();
        assertEquals(user, result.systemUser());
    }

    @Test
    void buildSetsCorrectAirTransportCompany() {
        final AirTransportCompany company = tapCompany();
        final PilotUser result = validBuilder().withAirTransportCompany(company).build();
        assertEquals(company, result.airTransportCompany());
    }

    @Test
    void buildSetsCorrectPhoneNumber() {
        final PilotUser result = validBuilder().build();
        assertEquals("+351999999999", result.phoneNumber().toString());
    }

    @Test
    void buildSetsCertifications() {
        final PilotUser result = validBuilder().build();
        assertFalse(result.pilotCertifications().isEmpty());
    }

    @Test
    void buildWithMultipleCertificationsStoresAll() {
        final PilotUser result = validBuilder()
                .withCertification(someCertification())
                .withCertification(someCertification())
                .build();
        assertEquals(3, result.pilotCertifications().size());
    }

    @Test
    void buildThrowsWhenNoCertificationsProvided() {
        assertThrows(IllegalStateException.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("+351999999999")
                        .build());
    }

    @Test
    void buildThrowsWhenSystemUserIsMissing() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenAirTransportCompanyIsMissing() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenSecurityDataIsMissing() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenSkillAssessmentIsMissing() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenPhoneNumberIsMissing() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("2024-01-01")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenSecurityDataIsInvalidDate() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("not-a-date")
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenSkillAssessmentIsInvalidDate() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("not-a-date")
                        .withPhoneNumber("+351999999999")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenPhoneNumberIsInvalid() {
        assertThrows(Exception.class, () ->
                new PilotUserBuilder()
                        .withSystemUser(buildUser("pilot@tap.com"))
                        .withAirTransportCompany(tapCompany())
                        .withSecurityData("2027-01-01")
                        .withSkillAssessment("2024-01-01")
                        .withPhoneNumber("invalid-phone")
                        .withCertification(someCertification())
                        .build());
    }

    @Test
    void buildThrowsWhenCertificationIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                validBuilder().withCertification(null));
    }

    @Test
    void buildThrowsWhenDueDateEndIsBeforeStart() {
        assertThrows(IllegalArgumentException.class, () ->
                DueDate.valueOf(LocalDate.of(2026, 1, 1), LocalDate.of(2024, 1, 1)));
    }
}