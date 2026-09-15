package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.TestAircraftRepositoryFactory;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;
import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ImportFlightPlanServiceTest {

    private final TestAircraftRepositoryFactory repos = new TestAircraftRepositoryFactory();
    private final InMemoryAirportRepository airports = new InMemoryAirportRepository();
    private final SimulatorJsonTestFixtures.InMemoryEngineModelRepository engines =
            SimulatorJsonTestFixtures.newEngineRepository();
    private ImportFlightPlanService service;
    private PilotUser pilot;

    @BeforeEach
    void setUp() {
        SimulatorJsonTestFixtures.seedA320(repos.aircraftModels(), engines);
        service = new ImportFlightPlanService(
                repos.flights(), repos.aircraft(), repos.aircraftModels(), engines, airports);
        airports.save(new Airport(
                AirportIATACode.valueOf("LIS"),
                AirportICAOCode.valueOf("LPPT"),
                Coordinates.valueOf(38.77, -9.13, 113.0),
                AreaCode.valueOf("PT-N")));
        airports.save(new Airport(
                AirportIATACode.valueOf("LHR"),
                AirportICAOCode.valueOf("EGLL"),
                Coordinates.valueOf(51.47, -0.45, 25.0),
                AreaCode.valueOf("GB-L")));
        repos.aircraft().save(sampleAircraft("CS-IMP1"));
        pilot = mock(PilotUser.class);
    }

    private static Aircraft sampleAircraft(final String registration) {
        return new Aircraft(
                AircraftRegistration.valueOf(registration),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine(),
                CabinConfiguration.ofEconomyBusinessFirst(180, 0, 0),
                new RegistrationCountry("PT"),
                IATACode.valueOf("TP"),
                OperationalStatus.ACTIVE,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - 5));
    }

    @Test
    void ensureValidImportPersistsFlight() throws IOException, URISyntaxException {
        final String dsl = Files.readString(resourcePath("dsl/valid.txt"));
        final ParseResult parsed = new FlightDslParser().parse(dsl);
        assertTrue(parsed.isValid());

        final ImportFlightPlanResult result = service.importFlightPlan(
                parsed.getDescriptor(), dsl, pilot, "CS-IMP1");

        assertTrue(result.isSuccess());
        final Flight saved = repos.flights().findByDesignator(new FlightDesignator("valid")).orElseThrow();
        assertEquals(FlightPlanStatus.DRAFT, saved.flightPlan().status());
        assertTrue(saved.flightPlan().hasDslContent());
        assertTrue(saved.flightPlan().hasJsonContent());
        assertTrue(saved.flightPlan().jsonContent().contains("\"Aircraft\""));
        assertTrue(saved.flightPlan().jsonContent().contains("\"DepartureAirport\""));
        assertEquals(pilot, saved.pilot());
        assertEquals("LPPT-EGLL", saved.routeName());
        assertNotNull(saved.schedule());
    }

    @Test
    void ensureDuplicateDesignatorFails() throws IOException, URISyntaxException {
        ensureValidImportPersistsFlight();

        final String dsl = Files.readString(resourcePath("dsl/valid.txt"));
        final ParseResult parsed = new FlightDslParser().parse(dsl);

        final ImportFlightPlanResult second = service.importFlightPlan(
                parsed.getDescriptor(), dsl, pilot, "CS-IMP1");

        assertFalse(second.isSuccess());
        assertTrue(second.message().toLowerCase().contains("already exists"));
    }

    @Test
    void ensureUnknownAircraftFails() throws IOException, URISyntaxException {
        final String dsl = Files.readString(resourcePath("dsl/valid.txt"));
        final ParseResult parsed = new FlightDslParser().parse(dsl);

        final ImportFlightPlanResult result = service.importFlightPlan(
                parsed.getDescriptor(), dsl, pilot, "CS-UNKNOWN");

        assertFalse(result.isSuccess());
        assertTrue(result.message().toLowerCase().contains("unknown aircraft"));
    }

    @Test
    void ensureNullPilotFails() throws IOException, URISyntaxException {
        final String dsl = Files.readString(resourcePath("dsl/valid.txt"));
        final ParseResult parsed = new FlightDslParser().parse(dsl);

        final ImportFlightPlanResult result = service.importFlightPlan(
                parsed.getDescriptor(), dsl, null, "CS-IMP1");

        assertFalse(result.isSuccess());
        assertTrue(result.message().toLowerCase().contains("pilot"));
    }

    private static Path resourcePath(final String classpathRelative) throws URISyntaxException {
        final var url = ImportFlightPlanServiceTest.class.getClassLoader().getResource(classpathRelative);
        assertNotNull(url);
        return Path.of(url.toURI());
    }
}
