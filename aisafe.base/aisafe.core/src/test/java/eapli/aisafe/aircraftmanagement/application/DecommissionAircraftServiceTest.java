package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.AircraftTestFixtures;
import eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightSchedule;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;

import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecommissionAircraftServiceTest {

    private TestAircraftRepositoryFactory repositoryFactory;
    private AircraftRepository aircraftRepository;
    private FlightRepository flightRepository;
    private DecommissionAircraftService service;

    @BeforeEach
    void setUp() {
        repositoryFactory = new TestAircraftRepositoryFactory();
        aircraftRepository = repositoryFactory.aircraft();
        flightRepository = repositoryFactory.flights();
        service = new DecommissionAircraftService(aircraftRepository, flightRepository);
    }

    @Test
    void constructorRejectsNullRepositories() {
        assertThrows(IllegalArgumentException.class, () -> new DecommissionAircraftService(null, flightRepository));
        assertThrows(IllegalArgumentException.class, () -> new DecommissionAircraftService(aircraftRepository, null));
    }

    @Test
    void listActiveFleetRequiresOwnerCompany() {
        assertThrows(NullPointerException.class, () -> service.listActiveFleet(null));
    }

    @Test
    void listActiveFleetDelegatesToRepository() {
        aircraftRepository.save(activeAircraft("CS-LIST1", "TP"));
        assertTrue(service.listActiveFleet(IATACode.valueOf("TP")).iterator().hasNext());
    }

    @Test
    void decommissionSucceedsWhenNoPendingFlights() {
        aircraftRepository.save(activeAircraft("CS-OK01", "TP"));

        final LocalDateTime now = LocalDateTime.of(2026, 6, 1, 12, 0);
        final Aircraft saved = service.decommission("cs-ok01", IATACode.valueOf("TP"), now);

        assertSame(OperationalStatus.DECOMMISSIONED, saved.operationalStatus());
    }

    @Test
    void decommissionFailsWhenPendingFlightsExist() {
        final LocalDateTime now = LocalDateTime.of(2026, 6, 1, 12, 0);
        aircraftRepository.save(activeAircraft("CS-PEND1", "TP"));
        final Flight pending = new Flight(new FlightDesignator("TPPEND1"), "R1", "CS-PEND1");
        pending.assignSchedule(new FlightSchedule(now, now.plusHours(2)));
        flightRepository.save(pending);

        assertThrows(AircraftHasPendingFlightsException.class,
                () -> service.decommission("CS-PEND1", IATACode.valueOf("TP"), now));
    }

    @Test
    void decommissionSucceedsWhenOnlyPastFlightsExist() {
        final LocalDateTime now = LocalDateTime.of(2026, 6, 1, 12, 0);
        aircraftRepository.save(activeAircraft("CS-PAST1", "TP"));
        final Flight past = new Flight(new FlightDesignator("TPPAST1"), "R1", "CS-PAST1");
        past.assignSchedule(new FlightSchedule(now.minusDays(2), now.minusDays(2).plusHours(1)));
        flightRepository.save(past);

        final Aircraft saved = service.decommission("CS-PAST1", IATACode.valueOf("TP"), now);
        assertSame(OperationalStatus.DECOMMISSIONED, saved.operationalStatus());
    }

    @Test
    void decommissionSucceedsWhenFlightHasNoSchedule() {
        final LocalDateTime now = LocalDateTime.of(2026, 6, 1, 12, 0);
        aircraftRepository.save(activeAircraft("CS-NSCH1", "TP"));
        flightRepository.save(new Flight(new FlightDesignator("TPNSCH1"), "R1", "CS-NSCH1"));

        final Aircraft saved = service.decommission("CS-NSCH1", IATACode.valueOf("TP"), now);
        assertSame(OperationalStatus.DECOMMISSIONED, saved.operationalStatus());
    }

    @Test
    void decommissionFailsWhenWrongCompany() {
        aircraftRepository.save(activeAircraft("CS-WRONG", "TP"));

        assertThrows(AircraftNotInCompanyFleetException.class,
                () -> service.decommission("CS-WRONG", IATACode.valueOf("FR"), LocalDateTime.now()));
    }

    @Test
    void decommissionRequiresOwnerCompany() {
        assertThrows(NullPointerException.class,
                () -> service.decommission("CS-TST", null, LocalDateTime.now()));
    }

    @Test
    void decommissionRequiresReferenceTime() {
        assertThrows(NullPointerException.class,
                () -> service.decommission("CS-TST", IATACode.valueOf("TP"), null));
    }

    @Test
    void decommissionRequiresRegistration() {
        assertThrows(NullPointerException.class,
                () -> service.decommission(null, IATACode.valueOf("TP"), LocalDateTime.now()));
    }

    @Test
    void decommissionFailsWhenUnknownRegistration() {
        assertThrows(AircraftNotFoundException.class,
                () -> service.decommission("CS-XXX", IATACode.valueOf("TP"), LocalDateTime.now()));
    }

    @Test
    void decommissionFailsWhenAlreadyDecommissioned() {
        final Aircraft ac = AircraftTestFixtures.sampleAircraft(
                "CS-RET01", "M1", "E1", IATACode.valueOf("TP"),
                OperationalStatus.DECOMMISSIONED, 10, 10);
        aircraftRepository.save(ac);

        assertThrows(AircraftAlreadyDecommissionedException.class,
                () -> service.decommission("CS-RET01", IATACode.valueOf("TP"), LocalDateTime.now()));
    }

    private static Aircraft activeAircraft(final String registration, final String ownerIata) {
        return AircraftTestFixtures.sampleAircraft(
                registration, "M1", "E1", IATACode.valueOf(ownerIata),
                OperationalStatus.ACTIVE, 10, 3);
    }
}
