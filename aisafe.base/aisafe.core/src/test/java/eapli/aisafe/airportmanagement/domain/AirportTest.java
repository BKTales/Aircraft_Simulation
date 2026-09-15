package eapli.aisafe.airportmanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportTest {

    private static final AirportIATACode IATA_LIS = AirportIATACode.valueOf("LIS");
    private static final AirportICAOCode ICAO_LPPT = AirportICAOCode.valueOf("LPPT");
    private static final Coordinates COORDS = Coordinates.valueOf(38.77, -9.13, 113.0);

    private Airport createSubject(final String iata, final String icao) {
        return new Airport(AirportIATACode.valueOf(iata), AirportICAOCode.valueOf(icao), COORDS, new AreaCode());
    }

    @Test
    void ensureAirportIsCreatedCorrectly() {
        final Airport airport = new Airport(IATA_LIS, ICAO_LPPT, COORDS, new AreaCode());

        assertNotNull(airport);
        assertEquals(IATA_LIS, airport.identity());
        assertEquals(ICAO_LPPT, airport.icaoCode());
        assertEquals(COORDS, airport.coordinates());
    }

    @Test
    void ensureAirControlAreaCodeIsStored() {
        final AreaCode area = new AreaCode();
        final Airport airport = new Airport(IATA_LIS, ICAO_LPPT, COORDS, area);

        assertEquals(area.getCode(), airport.airControlAreaCode());
    }

    @Test
    void ensureNullIataCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new Airport(null, ICAO_LPPT, COORDS, new AreaCode());
            }
        });
    }

    @Test
    void ensureNullIcaoCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new Airport(IATA_LIS, null, COORDS, new AreaCode());
            }
        });
    }

    @Test
    void ensureNullCoordinatesThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new Airport(IATA_LIS, ICAO_LPPT, null, new AreaCode());
            }
        });
    }

    @Test
    void ensureNullAreaCodeThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new Airport(IATA_LIS, ICAO_LPPT, COORDS, null);
            }
        });
    }

    @Test
    void ensureSameAsReturnsTrueForSameIata() {
        final Airport a = createSubject("OPO", "LPPR");
        final Airport b = createSubject("OPO", "LPPR");

        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentIata() {
        final Airport a = createSubject("LIS", "LPPT");
        final Airport b = createSubject("OPO", "LPPR");

        assertFalse(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForNull() {
        final Airport airport = createSubject("FAO", "LPFR");
        assertFalse(airport.sameAs(null));
    }

    @Test
    void ensureToStringContainsIataAndIcao() {
        final Airport airport = createSubject("LIS", "LPPT");
        final String s = airport.toString();

        assertTrue(s.contains("LIS"));
        assertTrue(s.contains("LPPT"));
    }
}
