package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirTransportCompanyServiceTest {
    private static final String VALID_NAME = "TAP Air Portugal";
    private static final String VALID_IATA = "TP";
    private static final String VALID_ICAO = "TAP";

    @Mock
    private AirTransportCompanyRepository mockRepository;

    @Test
    @DisplayName("US060 - constructor rejects null repository")
    void constructorRejectsNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new AirTransportCompanyService(null));
    }

    @Test
    @DisplayName("US060 - maps integrity violation to duplicate IATA")
    void registerCompanyMapsIntegrityViolationToIataWhenCodeExistsAfterSave() {
        when(mockRepository.existsByIataCode(any(IATACode.class))).thenReturn(false).thenReturn(true);
        when(mockRepository.existsByIcaoCode(any(ICAOCode.class))).thenReturn(false);
        when(mockRepository.save(any(AirTransportCompany.class)))
                .thenThrow(new IntegrityViolationException("dup"));

        final var service = new AirTransportCompanyService(mockRepository);

        assertThrows(IATACodeAlreadyExistsException.class,
                () -> service.registerCompany("A", "AA", "AAA"));
    }

    @Test
    @DisplayName("US060 - maps integrity violation to duplicate ICAO")
    void registerCompanyMapsIntegrityViolationToIcaoWhenCodeExistsAfterSave() {
        when(mockRepository.existsByIataCode(any(IATACode.class))).thenReturn(false);
        when(mockRepository.existsByIcaoCode(any(ICAOCode.class))).thenReturn(false).thenReturn(true);
        when(mockRepository.save(any(AirTransportCompany.class)))
                .thenThrow(new IntegrityViolationException("dup"));

        final var service = new AirTransportCompanyService(mockRepository);

        assertThrows(ICAOCodeAlreadyExistsException.class,
                () -> service.registerCompany("B", "BB", "BBB"));
    }

    @Test
    @DisplayName("US060 - propagates unknown integrity violation")
    void registerCompanyPropagatesIntegrityViolationWhenCauseUnknown() {
        when(mockRepository.existsByIataCode(any(IATACode.class))).thenReturn(false).thenReturn(false);
        when(mockRepository.existsByIcaoCode(any(ICAOCode.class))).thenReturn(false).thenReturn(false);
        when(mockRepository.save(any(AirTransportCompany.class)))
                .thenThrow(new IntegrityViolationException("constraint"));

        final var service = new AirTransportCompanyService(mockRepository);

        assertThrows(IntegrityViolationException.class,
                () -> service.registerCompany("C", "CC", "CCC"));
    }

    @Test
    @DisplayName("US060 AC1 - registers company when codes are unique")
    void registerCompanySucceedsWhenCodesAreUnique() {
        final var repo = new FakeAirTransportCompanyRepository();
        final var service = new AirTransportCompanyService(repo);

        final AirTransportCompany result = service.registerCompany(" tap air portugal ", " tp ", " tap ");

        assertEquals(IATACode.valueOf(VALID_IATA), result.identity());
        assertTrue(repo.existsByIataCode(IATACode.valueOf(VALID_IATA)));
        assertTrue(repo.existsByIcaoCode(ICAOCode.valueOf(VALID_ICAO)));
    }

    @Test
    @DisplayName("US060 AC2 - fails when IATA already exists")
    void registerCompanyFailsWhenIataAlreadyExists() {
        final var repo = new FakeAirTransportCompanyRepository();
        repo.save(new AirTransportCompany(CompanyName.valueOf(VALID_NAME), IATACode.valueOf(VALID_IATA), ICAOCode.valueOf(VALID_ICAO)));
        final var service = new AirTransportCompanyService(repo);

        assertThrows(IATACodeAlreadyExistsException.class,
                () -> service.registerCompany("Another Company", "TP", "ACP"));
    }

    @Test
    @DisplayName("US060 AC3 - fails when ICAO already exists")
    void registerCompanyFailsWhenIcaoAlreadyExists() {
        final var repo = new FakeAirTransportCompanyRepository();
        repo.save(new AirTransportCompany(CompanyName.valueOf(VALID_NAME), IATACode.valueOf(VALID_IATA), ICAOCode.valueOf(VALID_ICAO)));
        final var service = new AirTransportCompanyService(repo);

        assertThrows(ICAOCodeAlreadyExistsException.class,
                () -> service.registerCompany("Another Company", "AF", "TAP"));
    }

    @Test
    @DisplayName("US060 AC4 - rejects IATA with 1 letter")
    void registerCompanyRejectsIataWithOneLetter() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany(VALID_NAME, "T", VALID_ICAO));
    }

    @Test
    @DisplayName("US060 AC4 - rejects IATA with 3 letters")
    void registerCompanyRejectsIataWithThreeLetters() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany(VALID_NAME, "TAP", VALID_ICAO));
    }

    @Test
    @DisplayName("US060 AC5 - rejects ICAO with 1 letter")
    void registerCompanyRejectsIcaoWithOneLetter() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany(VALID_NAME, VALID_IATA, "T"));
    }

    @Test
    @DisplayName("US060 AC5 - rejects ICAO with 4 letters")
    void registerCompanyRejectsIcaoWithFourLetters() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany(VALID_NAME, VALID_IATA, "TAPA"));
    }

    @Test
    @DisplayName("US060 - rejects null name")
    void registerCompanyRejectsNullName() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany(null, VALID_IATA, VALID_ICAO));
    }

    @Test
    @DisplayName("US060 - rejects blank name")
    void registerCompanyRejectsBlankName() {
        final var service = new AirTransportCompanyService(new FakeAirTransportCompanyRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.registerCompany("   ", VALID_IATA, VALID_ICAO));
    }

    private static final class FakeAirTransportCompanyRepository implements AirTransportCompanyRepository {
        private final Map<IATACode, AirTransportCompany> byIata = new HashMap<>();
        private final Map<ICAOCode, AirTransportCompany> byIcao = new HashMap<>();

        @Override
        public Optional<AirTransportCompany> findByIataCode(final IATACode code) {
            return Optional.ofNullable(byIata.get(code));
        }

        @Override
        public Optional<AirTransportCompany> findByIcaoCode(final ICAOCode code) {
            return Optional.ofNullable(byIcao.get(code));
        }

        @Override
        public <S extends AirTransportCompany> S save(final S entity) {
            byIata.put(entity.identity(), entity);
            byIcao.put(entity.icaoCode(), entity);
            return entity;
        }

        @Override
        public Iterable<AirTransportCompany> findAll() {
            return byIata.values();
        }

        @Override
        public Optional<AirTransportCompany> ofIdentity(final IATACode id) {
            return findByIataCode(id);
        }

        @Override
        public boolean containsOfIdentity(final IATACode id) {
            return byIata.containsKey(id);
        }

        @Override
        public void deleteOfIdentity(final IATACode entityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(final AirTransportCompany entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long count() {
            return byIata.size();
        }
    }
}
