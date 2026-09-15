package eapli.aisafe.aircraftmanagement.domain;

import eapli.aisafe.aircraftmanagement.AircraftTestFixtures;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.flightmanagement.SimulatorJsonTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AircraftTest {

    private static Aircraft sampleAircraft() {
        return new Aircraft(
                new AircraftRegistration("CS-DMO"),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine(),
                CabinConfiguration.ofEconomyBusinessFirst(120, 8, 0),
                new RegistrationCountry("PT"),
                IATACode.valueOf("TP"),
                OperationalStatus.ACTIVE,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - 5));
    }

    @Test
    void ageInYearsIsDerivedFromYearOfManufacture() {
        final int yearsOld = 8;
        final Aircraft subject = new Aircraft(
                new AircraftRegistration("CS-AGE"),
                SimulatorJsonTestFixtures.sampleA320Model(),
                SimulatorJsonTestFixtures.sampleEngine(),
                CabinConfiguration.ofEconomyBusinessFirst(100, 0, 0),
                new RegistrationCountry("PT"),
                IATACode.valueOf("TP"),
                OperationalStatus.ACTIVE,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - yearsOld));

        assertEquals(yearsOld, subject.ageInYears());
        assertEquals(Year.now().getValue() - yearsOld, subject.yearOfManufacture());
    }

    @Test
    void ensureAircraftIsCreatedWithExpectedIdentity() {
        final AircraftRegistration reg = new AircraftRegistration("CS-DMO");
        final Aircraft subject = sampleAircraft();

        assertEquals(reg, subject.identity());
        assertEquals(OperationalStatus.ACTIVE, subject.operationalStatus());
        assertEquals(IATACode.valueOf("TP"), subject.ownerCompanyIata());
    }

    @Test
    void ensureConstructorRejectsNullArguments() {
        final AircraftRegistration reg = new AircraftRegistration("CS-AAA");
        final AircraftModel model = AircraftTestFixtures.stubModel("M1");
        final EngineModel engine = AircraftTestFixtures.stubEngine("E1");
        final CabinConfiguration cabin = CabinConfiguration.ofEconomyBusinessFirst(1, 0, 0);
        final RegistrationCountry country = new RegistrationCountry("PT");
        final IATACode iata = IATACode.valueOf("TP");
        final NumberOfFlightCrew crew = NumberOfFlightCrew.valueOf(1);

        final YearOfManufacture yearOfManufacture = YearOfManufacture.valueOf(Year.now().getValue() - 1);
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(null, model, engine, cabin, country, iata, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, null, engine, cabin, country, iata, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, null, cabin, country, iata, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, null, country, iata, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, cabin, null, iata, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, cabin, country, null, OperationalStatus.ACTIVE, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, cabin, country, iata, null, crew, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, cabin, country, iata, OperationalStatus.ACTIVE, null, yearOfManufacture));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(reg, model, engine, cabin, country, iata, OperationalStatus.ACTIVE, crew, null));
    }

    @Test
    void ensureSameAsBehaviour() {
        final Aircraft a = sampleAircraft();
        assertTrue(a.sameAs(a));
        assertFalse(a.sameAs(null));
        assertFalse(a.sameAs("other"));
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        final Aircraft subject = new Aircraft() {};
        assertNotNull(subject);
    }

    @Test
    void retireFromActiveServiceMarksDecommissioned() {
        final Aircraft subject = sampleAircraft();
        subject.retireFromActiveService();
        assertEquals(OperationalStatus.DECOMMISSIONED, subject.operationalStatus());
    }

    @Test
    void retireFromActiveServiceTwiceThrows() {
        final Aircraft subject = sampleAircraft();
        subject.retireFromActiveService();
        assertThrows(IllegalStateException.class, subject::retireFromActiveService);
    }

    @Test
    void assertAssignableToNewFlightFailsWhenDecommissioned() {
        final Aircraft subject = sampleAircraft();
        subject.retireFromActiveService();
        assertThrows(IllegalStateException.class, subject::assertAssignableToNewFlight);
    }

    @Test
    void assertAssignableToNewFlightSucceedsWhenActive() {
        sampleAircraft().assertAssignableToNewFlight();
    }
}
