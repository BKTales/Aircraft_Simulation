package eapli.aisafe.usermanagement.application;

import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.EmailDomain;
import eapli.aisafe.usermanagement.repositories.EmailDomainRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddUserServiceTest {

    private static class FakeEmailDomainRepository implements EmailDomainRepository {
        private final boolean domainExists;

        FakeEmailDomainRepository(boolean domainExists) {
            this.domainExists = domainExists;
        }

        @Override
        public Optional<EmailDomain> findByDomain(String domain) {
            return domainExists ? Optional.of(new EmailDomain(domain)) : Optional.empty();
        }

        @Override public <S extends EmailDomain> S save(S entity) { return entity; }
        @Override public Optional<EmailDomain> ofIdentity(String id) { return Optional.empty(); }
        @Override public boolean containsOfIdentity(String id) { return false; }
        @Override public void delete(EmailDomain entity) {}
        @Override public void deleteOfIdentity(String id) {}
        @Override public long count() { return 0; }
        @Override public Iterable<EmailDomain> findAll() { return java.util.List.of(); }
    }

    @Test
    void extractDomainFromEmailReturnsCorrectDomain() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertEquals("aisafe.admin.com", service.extractDomainFromEmail("user@aisafe.admin.com"));
    }

    @Test
    void extractDomainFromEmailWithSubdomain() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertEquals("mail.aisafe.com", service.extractDomainFromEmail("user@mail.aisafe.com"));
    }

    @Test
    void validateEmailDomainForAdminWithValidDomainReturnsValid() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateEmailDomain(Set.of(AISafeRoles.ADMIN), "user@aisafe.admin.com").isValid());
    }

    @Test
    void validateEmailDomainForBackofficeOperatorWithValidDomainReturnsValid() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateEmailDomain(Set.of(AISafeRoles.BACKOFFICE_OPERATOR), "user@aisafe.backoffice.com").isValid());
    }

    @Test
    void validateEmailDomainForWeatherPersonWithValidDomainReturnsValid() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateEmailDomain(Set.of(AISafeRoles.WEATHER_PERSON), "user@aisafe.weather.com").isValid());
    }

    @Test
    void validateEmailDomainForAdminWithInvalidDomainReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        final AddUserValidationResult result = service.validateEmailDomain(Set.of(AISafeRoles.ADMIN), "user@invalid.com");

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.INVALID_EMAIL_DOMAIN, result.outcome());
    }

    @Test
    void validateEmailDomainForBackofficeOperatorWithInvalidDomainReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        final AddUserValidationResult result = service.validateEmailDomain(
                Set.of(AISafeRoles.BACKOFFICE_OPERATOR), "user@invalid.com");

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.INVALID_EMAIL_DOMAIN, result.outcome());
    }

    @Test
    void validateEmailDomainForWeatherPersonWithInvalidDomainReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        final AddUserValidationResult result = service.validateEmailDomain(
                Set.of(AISafeRoles.WEATHER_PERSON), "user@invalid.com");

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.INVALID_EMAIL_DOMAIN, result.outcome());
    }

    @Test
    void validateEmailDomainForPilotSkipsDomainValidation() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        assertTrue(service.validateEmailDomain(Set.of(AISafeRoles.PILOT), "pilot@qualquerdominio.com").isValid());
    }

    @Test
    void validateEmailDomainForFlightControlOperatorSkipsDomainValidation() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        assertTrue(service.validateEmailDomain(Set.of(AISafeRoles.FLIGHT_CONTROL_OPERATOR), "fco@qualquerdominio.com").isValid());
    }

    @Test
    void validateEmailDomainForAirTransportCompanyCollaboratorSkipsDomainValidation() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        assertTrue(service.validateEmailDomain(
                Set.of(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR), "atc@qualquerdominio.com").isValid());
    }

    @Test
    void validateEmailDomainWithEmptyRolesReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        final AddUserValidationResult result = service.validateEmailDomain(Set.of(), "user@aisafe.admin.com");

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.NO_ROLE_SELECTED, result.outcome());
    }

    @Test
    void validateRegisterableRoleForAdminSucceeds() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateRegisterableRole(Set.of(AISafeRoles.ADMIN)).isValid());
    }

    @Test
    void validateRegisterableRoleForBackofficeOperatorSucceeds() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateRegisterableRole(Set.of(AISafeRoles.BACKOFFICE_OPERATOR)).isValid());
    }

    @Test
    void validateRegisterableRoleForWeatherPersonSucceeds() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        assertTrue(service.validateRegisterableRole(Set.of(AISafeRoles.WEATHER_PERSON)).isValid());
    }

    @Test
    void validateRegisterableRoleForPilotReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        final AddUserValidationResult result = service.validateRegisterableRole(Set.of(AISafeRoles.PILOT));

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.ROLE_NOT_REGISTERABLE, result.outcome());
    }

    @Test
    void validateRegisterableRoleWithEmptyRolesReturnsFailure() {
        final var service = new AddUserService(new FakeEmailDomainRepository(true));

        final AddUserValidationResult result = service.validateRegisterableRole(Set.of());

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.NO_ROLE_SELECTED, result.outcome());
    }

    @Test
    void validateRegistrationStopsAtRoleValidationWhenRoleIsInvalid() {
        final var service = new AddUserService(new FakeEmailDomainRepository(false));

        final AddUserValidationResult result = service.validateRegistration(
                Set.of(AISafeRoles.PILOT), "pilot@invalid.com");

        assertFalse(result.isValid());
        assertEquals(AddUserValidationResult.Outcome.ROLE_NOT_REGISTERABLE, result.outcome());
    }
}
