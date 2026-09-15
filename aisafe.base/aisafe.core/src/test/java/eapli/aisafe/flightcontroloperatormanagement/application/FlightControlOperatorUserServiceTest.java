package eapli.aisafe.flightcontroloperatormanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightControlOperatorUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FlightControlOperatorUserRepository flightControlOperatorUserRepository;

    @Mock
    private AirControlAreaRepository airControlAreaRepository;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AirControlArea area;

    private FlightControlOperatorUserService service;

    @BeforeEach
    void setUp() {
        service = new FlightControlOperatorUserService();
    }

    @Test
    void createCollaboratorUserBeginsAndCommitsTransaction() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(flightControlOperatorUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(mock(Role.class)), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01",  "+351999999999",
                userRepository, flightControlOperatorUserRepository, txCtx, airControlAreaRepository
        );

        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
    }

    @Test
    void createCollaboratorUserSavesSystemUserAndFCOUser() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(flightControlOperatorUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(mock(Role.class)), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01",  "+351999999999",
                userRepository, flightControlOperatorUserRepository, txCtx, airControlAreaRepository
        );

        verify(userRepository).save(any(SystemUser.class));
        verify(flightControlOperatorUserRepository).save(any(FlightControlOperatorUser.class));
    }

    @Test
    void createCollaboratorUserCreatesOperatorWithCorrectAreaCode() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(flightControlOperatorUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final ArgumentCaptor<FlightControlOperatorUser> captor =
                ArgumentCaptor.forClass(FlightControlOperatorUser.class);

        when(area.identity()).thenReturn(AreaCode.valueOf("TP"));
        when(airControlAreaRepository.ofIdentity(AreaCode.valueOf("TP")))
                .thenReturn(Optional.of(area));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(mock(Role.class)), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01",  "+351999999999",
                userRepository, flightControlOperatorUserRepository, txCtx, airControlAreaRepository
        );

        verify(flightControlOperatorUserRepository).save(captor.capture());
        assertEquals("TP", captor.getValue().airControlArea().identity().toString());
    }

    @Test
    void createCollaboratorUserWithoutTransactionalContextSkipsTransactionCalls() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(flightControlOperatorUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(mock(Role.class)), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01",  "+351999999999",
                userRepository, flightControlOperatorUserRepository, null, airControlAreaRepository
        );

        verifyNoInteractions(txCtx);
        verify(flightControlOperatorUserRepository).save(any(FlightControlOperatorUser.class));
    }

    @Test
    void createCollaboratorUserDoesNotCommitWhenPhoneNumberIsInvalid() {
        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        assertThrows(IllegalArgumentException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(mock(Role.class)), Calendar.getInstance(),
                        "TP", "2027-01-01", "2024-01-01",
                        "+351999999999999",
                        userRepository, flightControlOperatorUserRepository, txCtx, airControlAreaRepository
                )
        );

        verify(txCtx, never()).commit();
        verify(txCtx, never()).close();
    }

    @Test
    void createCollaboratorUserDoesNotCommitWhenSecurityDataDateIsInvalid() {
        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        assertThrows(DateTimeParseException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(mock(Role.class)), Calendar.getInstance(),
                        "TP",
                        "invalid-date",
                        "2024-04-04",
                        "+351999999999",
                        userRepository, flightControlOperatorUserRepository, txCtx, airControlAreaRepository
                )
        );

        verify(txCtx, never()).commit();
        verify(txCtx, never()).close();
    }

    @Test
    void findEligibleUsersForFCOReturnsUsersWithRoleNotYetRegisteredAsFCO() {
        final SystemUser eligible = mock(SystemUser.class);
        when(eligible.email()).thenReturn(EmailAddress.valueOf("eligible@mail.com"));
        when(eligible.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR)).thenReturn(true);

        final SystemUser withoutRole = mock(SystemUser.class);
        when(withoutRole.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR)).thenReturn(false);

        final SystemUser alreadyFco = mock(SystemUser.class);
        when(alreadyFco.email()).thenReturn(EmailAddress.valueOf("already@fco.com"));
        when(alreadyFco.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR)).thenReturn(true);

        when(userRepository.findByActive(true)).thenReturn(List.of(eligible, withoutRole, alreadyFco));

        final FlightControlOperatorUser existingFco = mock(FlightControlOperatorUser.class);
        final SystemUser existingFcoUser = mock(SystemUser.class);
        when(existingFcoUser.email()).thenReturn(EmailAddress.valueOf("already@fco.com"));
        when(existingFco.systemUser()).thenReturn(existingFcoUser);
        when(flightControlOperatorUserRepository.findAll()).thenReturn(List.of(existingFco));

        final List<SystemUser> result = service.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        assertEquals(1, result.size());
        assertEquals(EmailAddress.valueOf("eligible@mail.com"), result.get(0).email());
    }

    @Test
    void findEligibleUsersForFCOReturnsEmptyWhenAllUsersAlreadyRegistered() {
        final SystemUser alreadyFco = mock(SystemUser.class);
        when(alreadyFco.email()).thenReturn(EmailAddress.valueOf("already@fco.com"));
        when(alreadyFco.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR)).thenReturn(true);

        when(userRepository.findByActive(true)).thenReturn(List.of(alreadyFco));

        final FlightControlOperatorUser existingFco = mock(FlightControlOperatorUser.class);
        final SystemUser existingFcoUser = mock(SystemUser.class);
        when(existingFcoUser.email()).thenReturn(EmailAddress.valueOf("already@fco.com"));
        when(existingFco.systemUser()).thenReturn(existingFcoUser);
        when(flightControlOperatorUserRepository.findAll()).thenReturn(List.of(existingFco));

        final List<SystemUser> result = service.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        assertTrue(result.isEmpty());
    }

    @Test
    void findEligibleUsersForFCOReturnsEmptyWhenNoActiveUsers() {
        when(userRepository.findByActive(true)).thenReturn(Collections.emptyList());
        when(flightControlOperatorUserRepository.findAll()).thenReturn(Collections.emptyList());

        final List<SystemUser> result = service.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        assertTrue(result.isEmpty());
    }

    @Test
    void createFCOOnlyCreatesOperatorForEligibleUser() {
        final SystemUser eligible = mockUserWithRole("eligible@mail.com", true);
        when(userRepository.findByActive(true)).thenReturn(List.of(eligible));
        when(flightControlOperatorUserRepository.findAll()).thenReturn(Collections.emptyList());
        when(flightControlOperatorUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airControlAreaRepository.ofIdentity(any()))
                .thenReturn(Optional.of(area));

        final List<SystemUser> eligibleUsers = service.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        service.createFCOOnly(
                "eligible@mail.com",
                "AREA1",
                "2026-12-12",
                "2024-12-12",
                "+351912345678",
                eligibleUsers,
                flightControlOperatorUserRepository,
                txCtx,
                airControlAreaRepository
        );

        verify(flightControlOperatorUserRepository).save(any(FlightControlOperatorUser.class));
        verify(txCtx).commit();
    }

    @Test
    void createFCOOnlyThrowsWhenEmailNotInEligibleList() {
        when(userRepository.findByActive(true)).thenReturn(Collections.emptyList());
        when(flightControlOperatorUserRepository.findAll()).thenReturn(Collections.emptyList());

        service.findEligibleUsersForFCO(userRepository, flightControlOperatorUserRepository);

        assertThrows(EntityNotFoundException.class, () ->
                service.createFCOOnly(
                        "nonexistent@mail.com", "AREA1", "2024-12-12", "2024-12-12","+351912345678",
                        Collections.emptyList(), flightControlOperatorUserRepository, txCtx, airControlAreaRepository
                )
        );

        verify(flightControlOperatorUserRepository, never()).save(any());
        verify(txCtx, never()).commit();
    }

    @Test
    void findActiveFlightControlOperatorsByAreaDelegatesToRepository() {
        final List<FlightControlOperatorUser> expected = List.of(mock(FlightControlOperatorUser.class));
        when(flightControlOperatorUserRepository.findByAreaAndActive(area)).thenReturn(expected);

        final Iterable<FlightControlOperatorUser> result =
                service.findActiveFlightControlOperatorsByArea(flightControlOperatorUserRepository, area);

        assertEquals(expected, result);
        verify(flightControlOperatorUserRepository).findByAreaAndActive(area);
    }

    private SystemUser mockUserWithRole(final String email, final boolean hasRole) {
        final SystemUser user = mock(SystemUser.class);
        when(user.email()).thenReturn(EmailAddress.valueOf(email));
        when(user.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR)).thenReturn(hasRole);
        return user;
    }
}
