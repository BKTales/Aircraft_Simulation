package eapli.aisafe.flightcontroloperatormanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlightControlOperatorUserBuilderTest {

    @Mock
    private AirControlArea area;

    private SystemUser buildSystemUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.com", "Password1", "Pilot", "Tap", "pilot@tap.com")
                .build();
    }

    @Test
    void ensureThatBuildCreatesCollaboratorFromSystemUserData() {
        final FlightControlOperatorUser subject = new FlightControlOperatorUserBuilder()
                .withSystemUser(buildSystemUser())
                .withAirControlArea(area)
                .withSecurityData("2027-04-25")
                .withPhoneNumber("+351999999999")
                .withSkillAssessment("2006-01-21")
                .build();

        assertEquals("Pilot", subject.systemUser().name().firstName());
        assertEquals("Tap", subject.systemUser().name().lastName());
        assertTrue(subject.systemUser().isActive());
    }

    @Test
    void ensureThatMissingSystemUserThrows() {
        final FlightControlOperatorUserBuilder builder = new FlightControlOperatorUserBuilder()
                .withAirControlArea(area)
                .withSecurityData("2026-04-25");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void ensureThatAllWithMethodsReturnSameBuilder() {
        final FlightControlOperatorUserBuilder builder = new FlightControlOperatorUserBuilder();

        assertNotNull(builder.withSystemUser(buildSystemUser()));
        assertNotNull(builder.withAirControlArea(area));
        assertNotNull(builder.withSecurityData("2026-04-25"));
    }
}