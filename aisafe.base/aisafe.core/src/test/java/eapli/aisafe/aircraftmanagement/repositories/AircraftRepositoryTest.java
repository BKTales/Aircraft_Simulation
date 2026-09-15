package eapli.aisafe.aircraftmanagement.repositories;

import eapli.aisafe.aircraftmanagement.AircraftTestFixtures;
import eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AircraftRepositoryTest {

    private AircraftRepository repository;

    private static final IATACode TP = IATACode.valueOf("TP");
    private static final IATACode FR = IATACode.valueOf("FR");

    @BeforeEach
    void setUp() {
        repository = new TestAircraftRepositoryFactory().aircraft();
    }

    @Test
    void existsByRegistrationReturnsTrueWhenOfIdentityReturnsPresent() {
        final AircraftRegistration id = new AircraftRegistration("CS-99");
        repository.save(aircraft("CS-99", TP, OperationalStatus.ACTIVE, "A320", 150, 5));

        assertTrue(repository.existsByRegistration(id));
    }

    @Test
    void existsByRegistrationReturnsFalseWhenOfIdentityReturnsEmpty() {
        assertFalse(repository.existsByRegistration(new AircraftRegistration("CS-MISS")));
    }

    @Test
    void findByRegistrationReturnsSameOptionalAsOfIdentity() {
        final AircraftRegistration id = new AircraftRegistration("CS-88");
        repository.save(aircraft("CS-88", TP, OperationalStatus.ACTIVE, "A320", 150, 5));

        assertEquals(Optional.of(id), repository.findByRegistration(id).map(Aircraft::identity));
    }

    @Test
    void findByRegistrationReturnsEmptyWhenOfIdentityReturnsEmpty() {
        assertEquals(Optional.empty(), repository.findByRegistration(new AircraftRegistration("CS-EMPTY")));
    }

    @Test
    void findActiveByOwnerCompanyReturnsOnlyActiveFleetForIata() {
        repository.save(aircraft("CS-TP1", TP, OperationalStatus.ACTIVE, "A320", 150, 5));
        repository.save(aircraft("CS-TP2", TP, OperationalStatus.DECOMMISSIONED, "A320", 150, 10));
        repository.save(aircraft("CS-FR1", FR, OperationalStatus.ACTIVE, "A320", 150, 3));

        final var result = toList(repository.findActiveByOwnerCompany(TP));
        assertEquals(1, result.size());
        assertEquals("CS-TP1", result.get(0).identity().toString());
    }

    @Test
    void findByOwnerCompanyReturnsActiveAndDecommissioned() {
        repository.save(aircraft("CS-TP1", TP, OperationalStatus.ACTIVE, "A320", 150, 5));
        repository.save(aircraft("CS-TP2", TP, OperationalStatus.DECOMMISSIONED, "B737", 160, 8));
        repository.save(aircraft("CS-FR1", FR, OperationalStatus.ACTIVE, "A320", 140, 2));

        final var result = toList(repository.findByOwnerCompany(TP));
        assertEquals(2, result.size());
    }

    @Test
    void findByOwnerCompanyAndModelFiltersByModel() {
        repository.save(aircraft("CS-M1", TP, OperationalStatus.ACTIVE, "A320", 150, 5));
        repository.save(aircraft("CS-M2", TP, OperationalStatus.ACTIVE, "B737", 160, 6));

        final var result = toList(repository.findByOwnerCompanyAndModel(TP, AircraftModelId.valueOf("A320")));
        assertEquals(1, result.size());
        assertEquals("CS-M1", result.get(0).identity().toString());
    }

    @Test
    void findByOwnerCompanyAndCapacityFiltersByExactSeats() {
        repository.save(aircraft("CS-C1", TP, OperationalStatus.ACTIVE, "A320", 180, 5));
        repository.save(aircraft("CS-C2", TP, OperationalStatus.ACTIVE, "A320", 150, 3));

        final var result = toList(repository.findByOwnerCompanyAndCapacity(TP, 180));
        assertEquals(1, result.size());
        assertEquals(180, result.get(0).cabinConfiguration().totalSeats());
    }

    @Test
    void findByOwnerCompanyAndAgeFiltersByExactAge() {
        repository.save(aircraft("CS-A1", TP, OperationalStatus.ACTIVE, "A320", 150, 12));
        repository.save(aircraft("CS-A2", TP, OperationalStatus.ACTIVE, "A320", 150, 5));

        final var result = toList(repository.findByOwnerCompanyAndAge(TP, 12));
        assertEquals(1, result.size());
        assertEquals(12, result.get(0).ageInYears());
    }

    @Test
    void findByOwnerCompanyRejectsNullOwner() {
        assertThrows(NullPointerException.class, () -> repository.findByOwnerCompany(null));
    }

    @Test
    void findByOwnerCompanyAndModelRejectsNullArguments() {
        assertThrows(NullPointerException.class,
                () -> repository.findByOwnerCompanyAndModel(null, AircraftModelId.valueOf("A320")));
        assertThrows(NullPointerException.class, () -> repository.findByOwnerCompanyAndModel(TP, null));
    }

    @Test
    void findByOwnerCompanyAndCapacityRejectsNullOwner() {
        assertThrows(NullPointerException.class, () -> repository.findByOwnerCompanyAndCapacity(null, 100));
    }

    @Test
    void findByOwnerCompanyAndAgeRejectsNullOwner() {
        assertThrows(NullPointerException.class, () -> repository.findByOwnerCompanyAndAge(null, 5));
    }

    @Test
    void findActiveByOwnerCompanyRejectsNullOwner() {
        assertThrows(NullPointerException.class, () -> repository.findActiveByOwnerCompany(null));
    }

    private static Aircraft aircraft(final String registrationCode,
                                     final IATACode owner,
                                     final OperationalStatus status,
                                     final String modelId,
                                     final int seats,
                                     final int ageInYears) {
        return AircraftTestFixtures.sampleAircraft(
                registrationCode, modelId, "ENG1", owner, status, seats, ageInYears);
    }

    private static java.util.List<Aircraft> toList(final Iterable<Aircraft> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
}
