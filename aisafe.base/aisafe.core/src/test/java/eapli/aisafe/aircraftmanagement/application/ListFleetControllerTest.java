package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.Application;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
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
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFleetControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private AircraftService aircraftService;

    @Mock
    private CompanyCollaboratorUserRepository collaborators;

    @InjectMocks
    private ListFleetController controller;

    @AfterEach
    void tearDown() {
        resetAuthzRegistry();
        resetPersistenceFactory();
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new ListFleetController(null, aircraftService, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new ListFleetController(authz, null, collaborators));
        assertThrows(IllegalArgumentException.class, () -> new ListFleetController(authz, aircraftService, null));
    }

    @Test
    void listFleetDelegatesToServiceWithCollaboratorCompany() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final FleetListCriteria criteria = FleetListCriteria.unfiltered();
        final List<Aircraft> expected = List.of(mock(Aircraft.class));
        when(aircraftService.listFleet(IATACode.valueOf("TP"), criteria)).thenReturn(expected);

        assertSame(expected, controller.listFleet(criteria));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        verify(aircraftService).listFleet(IATACode.valueOf("TP"), criteria);
    }

    @Test
    void modelsUsedInCompanyFleetDelegatesToService() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final List<AircraftModel> expected = List.of(mock(AircraftModel.class));
        when(aircraftService.modelsUsedInFleet(IATACode.valueOf("TP"))).thenReturn(expected);

        assertSame(expected, controller.modelsUsedInCompanyFleet());
        verify(aircraftService).modelsUsedInFleet(IATACode.valueOf("TP"));
    }

    @Test
    void manufacturersUsedInCompanyFleetDelegatesToService() {
        final UserSession session = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser().identity()).thenReturn(Username.valueOf("atcc1"));

        final CompanyCollaboratorUser collaborator = mock(CompanyCollaboratorUser.class);
        when(collaborator.airTransportCompany())
                .thenReturn(new AirTransportCompany(CompanyName.valueOf("TAP"),
                        IATACode.valueOf("TP"), ICAOCode.valueOf("TAP")));
        when(collaborators.findByUsername(Username.valueOf("atcc1"))).thenReturn(Optional.of(collaborator));

        final List<ManufacturerId> expected = List.of(ManufacturerId.valueOf("MAN01"));
        when(aircraftService.manufacturersUsedInFleet(IATACode.valueOf("TP"))).thenReturn(expected);

        assertSame(expected, controller.manufacturersUsedInCompanyFleet());
        verify(aircraftService).manufacturersUsedInFleet(IATACode.valueOf("TP"));
    }

    @Test
    void defaultConstructorIsCovered() {
        configureRepositoryFactoryForDefaultConstructor();
        ensureAuthzRegistryConfigured();
        assertDoesNotThrow(() -> new ListFleetController());
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
