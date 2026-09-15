package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
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
class PilotUserCollaboratorUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyCollaboratorUserRepository collaboratorUserRepository;

    @Mock
    private PilotUserRepository pilotUserRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AirTransportCompany company;

    @Mock
    private AircraftModel aircraftModel;

    private PilotCollaboratorUserService service;
    private CompanyCollaboratorUserService collaboratorService;

    private static final String VALID_SECURITY_DATE = "2027-01-01";
    private static final String VALID_SKILLS_DATE = "2024-01-01";

    @BeforeEach
    void setUp() {
        service = new PilotCollaboratorUserService();
        collaboratorService = new CompanyCollaboratorUserService();
    }

    @Test
    void createPilotUserBeginsAndCommitsTransaction() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pilotUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aircraftModelRepository.ofIdentity(any())).thenReturn(Optional.of(aircraftModel));

        service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository , txCtx, aircraftModelRepository
        );

        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(txCtx).close();
    }

    @Test
    void createPilotUserSavesSystemUserAndCollaborator() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pilotUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aircraftModelRepository.ofIdentity(any())).thenReturn(Optional.of(aircraftModel));

        service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        verify(userRepository).save(any(SystemUser.class));
        verify(pilotUserRepository).save(any(PilotUser.class));
    }

    @Test
    void createPilotUserCreatesCollaboratorWithCorrectCompany() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pilotUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(company.identity()).thenReturn(IATACode.valueOf("TP"));
        when(aircraftModelRepository.ofIdentity(any())).thenReturn(Optional.of(aircraftModel));

        final ArgumentCaptor<PilotUser> captor =
                ArgumentCaptor.forClass(PilotUser.class);

        service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        verify(pilotUserRepository).save(captor.capture());
        assertEquals("TP", captor.getValue().airTransportCompany().identity().toString());
        assertEquals(1, captor.getValue().pilotCertifications().size());
    }

    @Test
    void createPilotUserWithoutTransactionalContextSkipsTransactionCalls() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pilotUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aircraftModelRepository.ofIdentity(any())).thenReturn(Optional.of(aircraftModel));

        service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, null, aircraftModelRepository
        );

        verifyNoInteractions(txCtx);
        verify(pilotUserRepository).save(any(PilotUser.class));
    }

    @Test
    void createPilotUserThrowsWhenRoleIsAtcc() {
         when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.ATCC),
                Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, null, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.INVALID_ROLE, result.outcome());

        verify(pilotUserRepository, never()).save(any());
    }

    @Test
    void createPilotUserDoesNotCommitWhenCertificationDateIsInvalid() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aircraftModelRepository.ofIdentity(any())).thenReturn(Optional.of(aircraftModel));

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "invalid-date", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );


        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.INVALID_DATE_FORMAT, result.outcome());

        verify(txCtx).rollback();
        verify(txCtx).close();
    }

    @Test
    void createPilotUserThrowsWhenUserHasMoreThanOneRole() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT, CompanyCollaboratorRoles.ATCC),
                Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, null, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.INVALID_ROLE, result.outcome());
        verify(pilotUserRepository, never()).save(any());
    }

    @Test
    void createPilotUserReturnsDuplicateUsernameWhenIntegrityViolationOccurs() {
        when(userRepository.save(any())).thenThrow(new jakarta.persistence.RollbackException(
                "Error", new eapli.framework.domain.repositories.IntegrityViolationException("Duplicado")));

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.DUPLICATE_USERNAME, result.outcome());
        verify(txCtx).rollback();
    }

    @Test
    void createPilotUserReturnsInvalidPhoneFormatWhenExceptionIsThrown() {
        when(userRepository.save(any())).thenAnswer(inv -> {
            throw new InvalidPhoneFormat("Invalid phone");
        });

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "bad-phone",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.INVALID_PHONE_FORMAT, result.outcome());
    }

    @Test
    void createPilotUserReturnsInvalidInputWhenPreconditionFails() {
        when(userRepository.save(any())).thenThrow(new IllegalArgumentException("Respective system user cannot be null"));

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.INVALID_INPUT, result.outcome());
        assertEquals("Respective system user cannot be null", result.message());
    }

    @Test
    void createPilotUserReturnsAircraftNotFoundWhenEntityDoesNotExist() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aircraftModelRepository.ofIdentity(any())).thenThrow(new EntityNotFoundException());

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                List.of(new PilotCertificationSpec("A320", "2024-01-01", "2025-01-01")),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        assertFalse(result.isSuccess());
        assertEquals(AddPilotResult.Outcome.AIRCRAFT_MODEL_NOT_FOUND, result.outcome());
    }

    @Test
    void createPilotUserReturnsNoCertificationsErrorWhenListIsEmpty() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddPilotResult result = service.createPilotUser(
                "pilot1", "Password1", "John", "Doe", "pilot@mail.com",
                Set.of(CompanyCollaboratorRoles.PILOT), Calendar.getInstance(),
                company, VALID_SECURITY_DATE, VALID_SKILLS_DATE, "+351999999999",
                Collections.emptyList(),
                userRepository, pilotUserRepository, txCtx, aircraftModelRepository
        );

        assertFalse(result.isSuccess(), "The result should indicate failure due to missing certifications.");
        assertEquals(AddPilotResult.Outcome.NO_CERTIFICATIONS, result.outcome());

        verify(txCtx).rollback();
        verify(txCtx).close();

        verify(pilotUserRepository, never()).save(any());
    }

    @Test
    void findATCCReturnsCollaboratorWhenExists() {
        final Username username = Username.valueOf("user1");
        final CompanyCollaboratorUser expected = mock(CompanyCollaboratorUser.class);

        when(collaboratorUserRepository.findByUsername(username))
                .thenReturn(Optional.of(expected));

        final CompanyCollaboratorUser result =
                collaboratorService.findATCC(collaboratorUserRepository, username);

        assertEquals(expected, result);
        verify(collaboratorUserRepository).findByUsername(username);
    }

    @Test
    void findATCCThrowsExceptionWhenCollaboratorDoesNotExist() {
        final Username username = Username.valueOf("user1");

        when(collaboratorUserRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                collaboratorService.findATCC(collaboratorUserRepository, username)
        );

        verify(collaboratorUserRepository).findByUsername(username);
    }
}