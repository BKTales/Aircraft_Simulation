package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.Application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
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
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecommissionAircraftControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private DecommissionAircraftService service;

    @Mock
    private CompanyCollaboratorUserRepository collaborators;

    @InjectMocks
    private DecommissionAircraftController controller;

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new DecommissionAircraftController(null, service, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new DecommissionAircraftController(authz, null, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new DecommissionAircraftController(authz, service, null));
    }

    @Test
    void listActiveCompanyAircraftDelegatesToServiceWithCollaboratorIata() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final Aircraft a = mock(Aircraft.class);
        final List<Aircraft> expected = List.of(a);
        when(service.listActiveFleet(IATACode.valueOf("TP"))).thenReturn(expected);

        assertSame(expected, controller.listActiveCompanyAircraft());
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        verify(service).listActiveFleet(IATACode.valueOf("TP"));
    }

    @Test
    void decommissionAircraftDelegatesToService() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final Aircraft expected = mock(Aircraft.class);
        when(service.decommission(eq("CS-TST"), eq(IATACode.valueOf("TP")), any()))
                .thenReturn(expected);

        assertSame(expected, controller.decommissionAircraft("CS-TST"));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final ArgumentCaptor<java.time.LocalDateTime> nowCaptor = ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        verify(service).decommission(eq("CS-TST"), eq(IATACode.valueOf("TP")), nowCaptor.capture());
        assertNotNull(nowCaptor.getValue());
    }

    @Test
    void listFailsWhenNotAuthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        assertThrows(IllegalStateException.class, controller::listActiveCompanyAircraft);
    }

    @Test
    void listFailsWhenNoSession() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, controller::listActiveCompanyAircraft);
    }

    @Test
    void listFailsWhenPrincipalIsNull() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(null);

        assertThrows(IllegalStateException.class, controller::listActiveCompanyAircraft);
    }

    @Test
    void listFailsWhenPrincipalIsNotUsername() {
        final UserSession session = mock(UserSession.class);
        final SystemUser sysUser = mock(SystemUser.class);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(sysUser);
        doAnswer(invocation -> new Object()).when(sysUser).identity();

        assertThrows(IllegalStateException.class, controller::listActiveCompanyAircraft);
    }

    @Test
    void listFailsWhenCollaboratorNotRegistered() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("unknown"));
        when(collaborators.findByUsername(Username.valueOf("unknown"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, controller::listActiveCompanyAircraft);
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();

        assertDoesNotThrow(() -> new DecommissionAircraftController());
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
