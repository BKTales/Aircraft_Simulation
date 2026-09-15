package eapli.aisafe.dsl;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.api.SimulatorAirportJsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorAirportJsonMapperTest {

    @Test
    void mapsAirportCoordinates() {
        final Airport airport = new Airport(
                AirportIATACode.valueOf("OPO"),
                AirportICAOCode.valueOf("LPPR"),
                Coordinates.valueOf(41.2481, -8.6814, 69.0),
                AreaCode.valueOf("PT-N"));

        final String json = SimulatorAirportJsonMapper.toJsonObject(airport);

        assertTrue(json.contains("\"Id\": \"OPO\""));
        assertTrue(json.contains("\"Icao\": \"LPPR\""));
        assertTrue(json.contains("\"Latitude\": 41.2481"));
        assertTrue(json.contains("\"Longitude\": -8.6814"));
        assertTrue(json.contains("\"Altitude\": 69"));
        assertTrue(json.contains("\"AreaCode\": \"PT-N\""));
    }
}
