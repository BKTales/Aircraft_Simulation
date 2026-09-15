package eapli.aisafe.airtransportcompanymanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirTransportCompanyTest {

    private AirTransportCompany createSubject(final String iata, final String name, final String icao) {
        return new AirTransportCompany(CompanyName.valueOf(name), IATACode.valueOf(iata), ICAOCode.valueOf(icao));
    }

    @Test
    void ensureAirTransportCompanyIsCreatedCorrectly() {
        final AirTransportCompany subject = createSubject("TP", "TAP Air Portugal", "TAP");

        assertNotNull(subject);
        assertEquals(IATACode.valueOf("TP"), subject.identity());
    }

    @Test
    void ensureConstructorRejectsNullComponents() {
        final IATACode iata = IATACode.valueOf("TP");
        final ICAOCode icao = ICAOCode.valueOf("TAP");
        final CompanyName name = CompanyName.valueOf("TAP Air Portugal");

        assertThrows(IllegalArgumentException.class, () -> new AirTransportCompany(null, iata, icao));
        assertThrows(IllegalArgumentException.class, () -> new AirTransportCompany(name, null, icao));
        assertThrows(IllegalArgumentException.class, () -> new AirTransportCompany(name, iata, null));
    }

    @Test
    void ensureSameAsWorksWithIdentity() {
        final AirTransportCompany a = createSubject("TP", "TAP Air Portugal", "TAP");
        final AirTransportCompany b = createSubject("TP", "Transportes Aereos Portugueses", "TAP");
        final AirTransportCompany c = createSubject("AF", "Air France", "AFR");

        assertTrue(a.sameAs(b));
        assertFalse(a.sameAs(c));
    }
}
