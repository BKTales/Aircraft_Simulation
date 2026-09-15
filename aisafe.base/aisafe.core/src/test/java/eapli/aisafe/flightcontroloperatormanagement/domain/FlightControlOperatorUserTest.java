package eapli.aisafe.flightcontroloperatormanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.companycollaboratormanagment.domain.*;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlightControlOperatorUserTest {

    @Mock
    private AirControlArea area;

    @Test
    void ensureThatConstructorRequiresAllFields() {
        final SystemUser user = buildUser("a@tap.com");
        final SecurityClearance clearance = SecurityClearance.valueOf(LocalDate.of(2026, 12, 25));
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.of(2026, 4, 25));
        final Phone phone = Phone.valueOf("+351999999999") ;

        assertThrows(IllegalArgumentException.class,
                () -> new FlightControlOperatorUser(null, clearance, area, phone,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new FlightControlOperatorUser(user, null, area, phone,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new FlightControlOperatorUser(user, clearance, null, phone,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new FlightControlOperatorUser(user, clearance, area, null,skillsAssessment));

        assertThrows(IllegalArgumentException.class,
                () -> new FlightControlOperatorUser(user, clearance, area, phone,null));
    }

    @Test
    void ensureThatIsActiveReflectsSystemUser() {
        final FlightControlOperatorUser activeUser =
                new FlightControlOperatorUser(
                        buildUser("a@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        final FlightControlOperatorUser inactiveUser =
                new FlightControlOperatorUser(
                        buildUser("b@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
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

        final FlightControlOperatorUser subject =
                new FlightControlOperatorUser(
                        user,
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
                        Phone.valueOf("+351999999999") ,
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        user.deactivate(Calendar.getInstance());

        assertFalse(subject.systemUser().isActive());
    }

    @Test
    void ensureThatSystemUserEmailIsCorrect() {
        final SystemUser user = buildUser("pilot@tap.com");

        final FlightControlOperatorUser subject =
                new FlightControlOperatorUser(
                        user,
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25))
                );

        assertEquals("pilot@tap.com", subject.systemUser().email().toString());
    }

    @Test
    void ensureThatGettersExposeValues() {
        final SystemUser user = buildUser("a@tap.com");
        final SecurityClearance clearance = SecurityClearance.valueOf(LocalDate.of(2026, 12, 25));
        final Phone phone = Phone.valueOf("+351999999999");
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.of(2026,4,25));

        final FlightControlOperatorUser subject =
                new FlightControlOperatorUser(user, clearance, area, phone,skillsAssessment);

        assertSame(area, subject.airControlArea());
        assertEquals(clearance, subject.securityClearance());
        assertEquals(user, subject.systemUser());
    }

    @Test
    void ensureThatSameAsUsesIdentity() {
        final SystemUser user = buildUser("a@tap.com");

        final FlightControlOperatorUser a =
                new FlightControlOperatorUser(user,
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        final FlightControlOperatorUser b =
                new FlightControlOperatorUser(user,
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
                        Phone.valueOf("+351999999999"),
                        SkillsAssessment.valueOf(LocalDate.of(2026,4,25)));

        final FlightControlOperatorUser c =
                new FlightControlOperatorUser(
                        buildUser("different@tap.com"),
                        SecurityClearance.valueOf(LocalDate.of(2026, 12, 25)),
                        area,
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
        final FlightControlOperatorUser instance = new FlightControlOperatorUser();

        assertNotNull(instance);
    }

    // helper
    private SystemUser buildUser(String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "password", "John", "Doe", email)
                .build();
    }
}