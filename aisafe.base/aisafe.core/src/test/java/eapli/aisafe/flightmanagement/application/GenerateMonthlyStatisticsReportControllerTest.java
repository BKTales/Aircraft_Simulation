package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.Application;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportResult;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateMonthlyStatisticsReportControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock
    private GenerateMonthlyStatisticsReportService service;
    @Mock
    private FlightControlOperatorUserRepository operators;
    @Mock
    private UserSession session;
    @Mock
    private SystemUser systemUser;
    @Mock
    private FlightControlOperatorUser operator;
    @Mock
    private AirControlArea area;

    private GenerateMonthlyStatisticsReportController controller;

    @BeforeEach
    void setUp() {
        controller = new GenerateMonthlyStatisticsReportController(authz, service, operators);
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();
        assertDoesNotThrow(() -> new GenerateMonthlyStatisticsReportController());
    }

    @Test
    void controllerRejectsNonFco() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);

        assertThrows(IllegalStateException.class, () -> controller.generate(2026, 6));
    }

    @Test
    void controllerUsesFcoAssignedArea() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("fco1"));
        when(operators.findByUsername(Username.valueOf("fco1"))).thenReturn(Optional.of(operator));
        when(operator.airControlArea()).thenReturn(area);
        when(area.identity()).thenReturn(AreaCode.valueOf("AREA-0"));

        final MonthlyReportResult expected = new MonthlyReportResult(Path.of("reports/monthly/AREA-0/2026-06-statistics.txt"), "content");
        when(service.generateMonthlyReport("AREA-0", java.time.YearMonth.of(2026, 6))).thenReturn(expected);

        final MonthlyReportResult result = controller.generate(2026, 6);

        assertEquals(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.FLIGHT_CONTROL_OPERATOR);
    }

    @Test
    void assignedAreaCodeUsesFcoSession() {
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.identity()).thenReturn(Username.valueOf("fco1"));
        when(operators.findByUsername(Username.valueOf("fco1"))).thenReturn(Optional.of(operator));
        when(operator.airControlArea()).thenReturn(area);
        when(area.identity()).thenReturn(AreaCode.valueOf("AREA-0"));

        assertEquals("AREA-0", controller.assignedAreaCode());
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new GenerateMonthlyStatisticsReportController(null, service, operators));
        assertThrows(IllegalArgumentException.class,
                () -> new GenerateMonthlyStatisticsReportController(authz, null, operators));
        assertThrows(IllegalArgumentException.class,
                () -> new GenerateMonthlyStatisticsReportController(authz, service, null));
    }

    private static void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties props = (Properties) propertiesField.get(Application.settings());
            props.setProperty("persistence.repositoryFactory",
                    "eapli.aisafe.flightmanagement.Us112TestRepositoryFactory");

            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void ensureAuthzRegistryConfigured() {
        try {
            AuthzRegistry.authorizationService();
        } catch (IllegalStateException ignored) {
            AuthzRegistry.configure(
                    mock(UserRepository.class),
                    mock(PasswordPolicy.class),
                    mock(PasswordEncoder.class));
        }
    }
}
