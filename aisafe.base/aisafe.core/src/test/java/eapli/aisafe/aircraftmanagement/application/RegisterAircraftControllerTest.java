package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.Application;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAircraftControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private AircraftService aircraftService;

    @Mock
    private CompanyCollaboratorUserRepository collaborators;

    @InjectMocks
    private RegisterAircraftController controller;

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAircraftController(null, aircraftService, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new RegisterAircraftController(authz, null, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new RegisterAircraftController(authz, aircraftService, null));
    }

    @Test
    void registerDelegatesToServiceWithCollaboratorCompanyIata() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final Aircraft expected = mock(Aircraft.class);
        when(aircraftService.registerAircraft(
                eq("CS-REG1"),
                eq("M1"),
                eq("ENG1"),
                any(CabinConfiguration.class),
                eq("PT"),
                eq(3),
                eq(2015),
                eq(IATACode.valueOf("TP"))))
                .thenReturn(expected);

        final Aircraft result = controller.registerAircraft(
                "CS-REG1", "M1", "ENG1", 10, 5, 0, "PT", 3, 2015);

        assertSame(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final ArgumentCaptor<CabinConfiguration> cabinCaptor = ArgumentCaptor.forClass(CabinConfiguration.class);
        verify(aircraftService).registerAircraft(
                eq("CS-REG1"),
                eq("M1"),
                eq("ENG1"),
                cabinCaptor.capture(),
                eq("PT"),
                eq(3),
                eq(2015),
                eq(IATACode.valueOf("TP")));
        final CabinConfiguration cabin = cabinCaptor.getValue();
        assertEquals(15, cabin.totalSeats());
    }

    @Test
    void registerFailsWhenNotAuthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        assertThrows(IllegalStateException.class,
                () -> controller.registerAircraft("CS-X", "M1", "E1", 1, 0, 0, "PT", 2, 2020));
    }

    @Test
    void registerFailsWhenNoSession() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> controller.registerAircraft("CS-X", "M1", "E1", 1, 0, 0, "PT", 2, 2020));
    }

    @Test
    void registerFailsWhenPrincipalIsNull() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> controller.registerAircraft("CS-X", "M1", "E1", 1, 0, 0, "PT", 2, 2020));
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> new RegisterAircraftController());
    }

    @Test
    void registerFailsWhenCollaboratorNotRegistered() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("unknown"));
        when(collaborators.findByUsername(Username.valueOf("unknown"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> controller.registerAircraft("CS-X", "M1", "E1", 1, 0, 0, "PT", 2, 2020));
    }

    private void configureRepositoryFactoryForDefaultConstructor() {
        try {
            final Field propertiesField = Application.settings().getClass().getDeclaredField("applicationProperties");
            propertiesField.setAccessible(true);
            final Properties props = (Properties) propertiesField.get(Application.settings());
            props.setProperty("persistence.repositoryFactory",
                    "eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory");

            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void ensureAuthzRegistryConfigured() {
        try {
            AuthzRegistry.authorizationService();
        } catch (IllegalStateException ignored) {
            AuthzRegistry.configure(mock(UserRepository.class), mock(PasswordPolicy.class), mock(PasswordEncoder.class));
        }
    }

    private void resetAuthzRegistry() {
        try {
            final Field authSvc = AuthzRegistry.class.getDeclaredField("authorizationSvc");
            authSvc.setAccessible(true);
            authSvc.set(null, null);
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private void resetPersistenceFactory() {
        try {
            final Field factoryField = PersistenceContext.class.getDeclaredField("theFactory");
            factoryField.setAccessible(true);
            factoryField.set(null, null);
        } catch (Exception ignored) {
            // best-effort
        }
    }
}
