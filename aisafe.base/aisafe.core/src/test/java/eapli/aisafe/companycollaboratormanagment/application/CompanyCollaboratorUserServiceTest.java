package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
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
class CompanyCollaboratorUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyCollaboratorUserRepository collaboratorRepository;

    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AirTransportCompany company;

    private CompanyCollaboratorUserService service;

    @BeforeEach
    void setUp() {
        service = new CompanyCollaboratorUserService();
    }

    @Test
    void createCollaboratorUserBeginsAndCommitsTransaction() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01", "+351999999999",
                userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository
        );

        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
    }

    @Test
    void createCollaboratorUserSavesSystemUserAndCollaborator() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01", "+351999999999",
                userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository
        );

        verify(userRepository).save(any(SystemUser.class));
        verify(collaboratorRepository).save(any(CompanyCollaboratorUser.class));
    }

    @Test
    void createCollaboratorUserCreatesCollaboratorWithCorrectIataCode() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(company.identity()).thenReturn(IATACode.valueOf("TP"));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        final ArgumentCaptor<CompanyCollaboratorUser> captor =
                ArgumentCaptor.forClass(CompanyCollaboratorUser.class);

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01", "+351999999999",
                userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository
        );

        verify(collaboratorRepository).save(captor.capture());
        assertEquals("TP", captor.getValue().airTransportCompany().identity().toString());
    }

    @Test
    void createCollaboratorUserWithoutTransactionalContextSkipsTransactionCalls() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01", "+351999999999",
                userRepository, collaboratorRepository, null, airTransportCompanyRepository
        );

        verifyNoInteractions(txCtx);
        verify(collaboratorRepository).save(any(CompanyCollaboratorUser.class));
    }

    @Test
    void createCollaboratorUserRegistersAsATCCWhenRoleIsATCC() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        final ArgumentCaptor<CompanyCollaboratorUser> captor =
                ArgumentCaptor.forClass(CompanyCollaboratorUser.class);

        service.createCollaboratorUser(
                "user1", "Password1", "John", "Doe", "john@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(),
                "TP", "2027-01-01", "2024-01-01", "+351999999999",
                userRepository, collaboratorRepository, null, airTransportCompanyRepository
        );

        verify(collaboratorRepository).save(captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    void createCollaboratorUserThrowsWhenRoleIsPilot() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        assertThrows(IllegalArgumentException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                        "TP", "2027-01-01", "2024-01-01", "+351999999999",
                        userRepository, collaboratorRepository, null, airTransportCompanyRepository
                )
        );

        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    void createCollaboratorUserDoesNotCommitWhenSecurityDataDateIsInvalid() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        assertThrows(DateTimeParseException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(), // <-- ATCC em vez de mock(Role.class)
                        "TP", "invalid-date", "2024-4-4", "+351999999999",
                        userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository
                )
        );

        verify(txCtx, never()).commit();
        verify(txCtx, never()).close();
    }

    @Test
    void createCollaboratorUserDoesNotCommitWhenPhoneNumberIsInvalid() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));

        assertThrows(IllegalArgumentException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(CompanyCollaboratorRoles.ATCC), Calendar.getInstance(), // <-- ATCC em vez de mock(Role.class)
                        "TP", "2027-01-01", "2024-01-01", "+35199999999999999",
                        userRepository, collaboratorRepository, txCtx, airTransportCompanyRepository
                )
        );

        verify(txCtx, never()).commit();
        verify(txCtx, never()).close();
    }

    @Test
    void createCollaboratorUserThrowsWhenUserHasMoreThanOneRole() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createCollaboratorUser(
                        "user1", "Password1", "John", "Doe", "john@mail.com",
                        Set.of(CompanyCollaboratorRoles.ATCC, CompanyCollaboratorRoles.PILOT),
                        Calendar.getInstance(),
                        "TP", "2027-01-01", "2024-01-01", "+351999999999",
                        userRepository, collaboratorRepository, null, airTransportCompanyRepository
                )
        );

        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    void findEligibleUsersForATCCReturnsUsersWithRoleNotYetRegistered() {
        final SystemUser pilot = mockUserWithRole("pilot@mail.com", true);
        final SystemUser operator = mockUserWithRole("op@mail.com", true);
        final SystemUser alreadyAtcc = mockUserWithRole("already@mail.com", true);

        when(userRepository.findByActive(true)).thenReturn(List.of(pilot, operator, alreadyAtcc));

        final CompanyCollaboratorUser existing = mock(CompanyCollaboratorUser.class);
        when(existing.systemUser()).thenReturn(alreadyAtcc);
        when(collaboratorRepository.findAll()).thenReturn(List.of(existing));

        final List<SystemUser> result = service.findEligibleUsersForATCC(userRepository, collaboratorRepository);

        assertEquals(2, result.size());
        assertFalse(result.contains(alreadyAtcc));
    }

    @Test
    void findEligibleUsersForATCCReturnsEmptyWhenAllAlreadyRegistered() {
        final SystemUser alreadyAtcc = mockUserWithRole("already@mail.com", true);

        when(userRepository.findByActive(true)).thenReturn(List.of(alreadyAtcc));

        final CompanyCollaboratorUser existing = mock(CompanyCollaboratorUser.class);
        when(existing.systemUser()).thenReturn(alreadyAtcc);
        when(collaboratorRepository.findAll()).thenReturn(List.of(existing));

        final List<SystemUser> result = service.findEligibleUsersForATCC(userRepository, collaboratorRepository);

        assertTrue(result.isEmpty());
    }

    @Test
    void findEligibleUsersForATCCReturnsEmptyWhenNoActiveUsers() {
        when(userRepository.findByActive(true)).thenReturn(Collections.emptyList());
        when(collaboratorRepository.findAll()).thenReturn(Collections.emptyList());

        final List<SystemUser> result = service.findEligibleUsersForATCC(userRepository, collaboratorRepository);

        assertTrue(result.isEmpty());
    }

    @Test
    void createATCCOnlyCreatesCollaboratorForEligibleUser() {
        final SystemUser eligible = mockUserWithRole("eligible@mail.com", true);
        when(eligible.roleTypes()).thenReturn(Set.of(CompanyCollaboratorRoles.ATCC));

        when(userRepository.findByActive(true)).thenReturn(List.of(eligible));
        when(collaboratorRepository.findAll()).thenReturn(Collections.emptyList());
        when(collaboratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(airTransportCompanyRepository.ofIdentity(any())).thenReturn(Optional.of(company));
        final List<SystemUser> eligibleUsers = service.findEligibleUsersForATCC(userRepository, collaboratorRepository);
        service.createATCCOnly(
                "eligible@mail.com", "TP", "2027-01-01", "2024-01-01", "+351912345678",eligibleUsers,
                collaboratorRepository, txCtx, airTransportCompanyRepository
        );

        verify(collaboratorRepository).save(any(CompanyCollaboratorUser.class));
        verify(txCtx).commit();
    }

    @Test
    void createATCCOnlyThrowsWhenEmailNotInEligibleList() {
        when(userRepository.findByActive(true)).thenReturn(Collections.emptyList());
        when(collaboratorRepository.findAll()).thenReturn(Collections.emptyList());

        service.findEligibleUsersForATCC(userRepository, collaboratorRepository);

        assertThrows(EntityNotFoundException.class, () ->
                service.createATCCOnly(
                        "nonexistent@mail.com", "TP", "2027-01-01", "2024-01-01", "+351912345678",
                        Collections.emptyList(),collaboratorRepository, txCtx, airTransportCompanyRepository
                )
        );

        verify(collaboratorRepository, never()).save(any());
        verify(txCtx, never()).commit();
    }

    @Test
    void findActiveCollaboratorsByCompanyDelegatesToRepository() {
        final List<CompanyCollaboratorUser> expected = List.of(mock(CompanyCollaboratorUser.class));
        when(collaboratorRepository.findATCCByCompanyAndActive(company)).thenReturn(expected);

        final Iterable<CompanyCollaboratorUser> result =
                service.findActiveCollaboratorsByCompany(collaboratorRepository, company);

        assertEquals(expected, result);
        verify(collaboratorRepository).findATCCByCompanyAndActive(company);
    }

    private SystemUser mockUserWithRole(final String email, final boolean hasRole) {
        final SystemUser user = mock(SystemUser.class);
        when(user.email()).thenReturn(EmailAddress.valueOf(email));
        when(user.hasAny(AISafeRoles.PILOT, AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)).thenReturn(hasRole);
        return user;
    }
}