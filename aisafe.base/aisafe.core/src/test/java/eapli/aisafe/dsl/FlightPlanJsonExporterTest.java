package eapli.aisafe.dsl;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircraftmodelmanagement.domain.AerodynamicCoefficients;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;
import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfEngines;
import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfSeats;
import eapli.aisafe.aircraftmodelmanagement.domain.PerformanceSpec;
import eapli.aisafe.aircraftmodelmanagement.domain.WeightSpecification;
import eapli.aisafe.aircraftmodelmanagement.domain.WingGeometry;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.FlightPlanJsonExporter;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.api.SimulatorFlightPlanJsonContext;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.enginemodelmanagement.domain.EngineName;
import eapli.aisafe.enginemodelmanagement.domain.FuelType;
import eapli.aisafe.enginemodelmanagement.domain.MotorizationType;
import eapli.aisafe.enginemodelmanagement.domain.TSFC;
import eapli.aisafe.enginemodelmanagement.domain.ThrustProfile;
import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightPlanJsonExporterTest {

    private final FlightDslParser parser = new FlightDslParser();

    @Test
    void ensureClimbSegmentEndAltitudeIsCruiseLevel() {
        final AltitudeSlotDescriptor ground = new AltitudeSlotDescriptor(69, 500);
        final AltitudeSlotDescriptor cruise = new AltitudeSlotDescriptor(11000, 500);
        final SegmentDescriptor climb = new SegmentDescriptor(
                41.24, -8.68, 41.50, -8.00,
                List.of(ground, cruise),
                90, 10.5);
        final SegmentDescriptor cruiseSegment = new SegmentDescriptor(
                41.50, -8.00, 41.76, -7.32,
                List.of(cruise),
                90, 10.5);
        final AltitudeSlotDescriptor destGround = new AltitudeSlotDescriptor(25, 500);
        final SegmentDescriptor descend = new SegmentDescriptor(
                41.76, -7.32, 38.77, -9.13,
                List.of(cruise, destGround),
                90, 10.5);
        final LegDescriptor leg = new LegDescriptor(
                "OPO", "2026-06-01 10:00",
                "LIS", "2026-06-01 12:00",
                new RouteDescriptor(List.of(climb, cruiseSegment, descend)),
                5000, "kg");
        final FlightPlanDescriptor descriptor = new FlightPlanDescriptor("TP123", "regular", 120, 10500.0, List.of(leg));
        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId("A320")
                .aircraftModel(sampleModel())
                .engineModel(sampleEngine())
                .airport(sampleAirport("OPO", "LPPR", 41.24, -8.68, 69.0))
                .airport(sampleAirport("LIS", "LPPT", 38.77, -9.13, 113.0))
                .build();

        final String json = FlightPlanJsonExporter.toSimulatorJson(descriptor, context);

        assertTrue(json.contains("\"Mode\": \"climb\""));
        assertTrue(json.contains(
                "\"Start\": {\n            \"Latitude\": 41.24,\n            \"Longitude\": -8.68,\n            \"Altitude\": { \"Quantity\": 69, \"Unit\": \"m\" }"));
        assertTrue(json.contains(
                "\"End\": {\n            \"Latitude\": 41.5,\n            \"Longitude\": -8,\n            \"Altitude\": { \"Quantity\": 11000, \"Unit\": \"m\" }"));
    }

    @Test
    void ensureGeneratedJsonHasNoStrayCommasAfterLoadRelocation() {
        final LegDescriptor leg = new LegDescriptor(
                "OPO", "2026-06-22 10:00",
                "LIS", "2026-06-22 13:00",
                new RouteDescriptor(List.of()),
                13000, "kg");
        final FlightPlanDescriptor descriptor = new FlightPlanDescriptor(
                "TP1001A", "regular", 200, 10000.0, 5000.0, List.of(leg));
        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId("A320")
                .aircraftModel(sampleModel())
                .engineModel(sampleEngine())
                .airport(sampleAirport("OPO", "LPPR", 41.24, -8.68, 69.0))
                .airport(sampleAirport("LIS", "LPPT", 38.77, -9.13, 113.0))
                .build();

        final String json = FlightPlanJsonExporter.toSimulatorJson(descriptor, context);

        assertFalse(json.contains("},\n,"), "leg fields must not contain an empty JSON element");
        assertTrue(json.contains("\"CargoWeight\": { \"Quantity\": 5000"));
        assertTrue(json.contains("\"PassengerWeight\": { \"Quantity\": 10000"));
    }

    @Test
    void ensureJsonMatchesSimulatorShape() throws IOException, URISyntaxException {
        final String dsl = Files.readString(resourcePath("dsl/valid.txt"));
        final ParseResult result = parser.parse(dsl);
        assertTrue(result.isValid());

        final FlightPlanDescriptor d = result.getDescriptor();
        final SimulatorFlightPlanJsonContext context = SimulatorFlightPlanJsonContext.builder()
                .aircraftId("A320")
                .aircraftModel(sampleModel())
                .engineModel(sampleEngine())
                .airport(sampleAirport("LIS", "LPPT", 38.77, -9.13, 113.0))
                .airport(sampleAirport("LHR", "EGLL", 51.47, -0.45, 25.0))
                .build();
        final String json = FlightPlanJsonExporter.toSimulatorJson(d, context);

        assertTrue(json.contains("\"FlightProfile\""));
        assertTrue(json.contains("\"Climb\""));
        assertTrue(json.contains("\"Descend\""));
        assertFalse(json.contains("\"Cruise\""));
        assertTrue(json.contains("\"DepartureAirport\""));
        assertTrue(json.contains("\"ArrivalAirport\""));
        assertTrue(json.contains("\"Aircraft\""));
        assertTrue(json.contains("\"AircraftId\": \"A320\""));
        assertTrue(json.contains("\"Mode\": \"climb\""));
        assertNotNull(json);
    }

    private static AircraftModel sampleModel() {
        final Manufacturer manufacturer = new Manufacturer(
                ManufacturerId.valueOf("MAN02"),
                new ManufacturerName("Airbus"),
                new CountryCode("PT"));
        return new AircraftModel(
                AircraftModelId.valueOf("A320"),
                ModelName.valueOf("Airbus A320"),
                AircraftType.PASSENGER,
                manufacturer,
                WeightSpecification.valueOf(78000, 60000, 42000),
                WingGeometry.valueOf(122.6, 35.8),
                AerodynamicCoefficients.valueOf(0.02, 1.5),
                PerformanceSpec.valueOf(12000, 230, 24000, 5200),
                NumberOfEngines.valueOf(2),
                NumberOfSeats.valueOf(180));
    }

    private static EngineModel sampleEngine() {
        return new EngineModel(
                EngineModelId.valueOf("MAN02-PW1100G"),
                EngineName.valueOf("PW1100G"),
                TSFC.valueOf(0.50),
                FuelType.JET_A1,
                ThrustProfile.valueOf(150.0, 135.0),
                new Manufacturer(
                        ManufacturerId.valueOf("MAN02"),
                        new ManufacturerName("Airbus"),
                        new CountryCode("PT")),
                MotorizationType.TURBOFAN);
    }

    private static Airport sampleAirport(final String iata, final String icao,
                                         final double lat, final double lon, final double elev) {
        return new Airport(
                AirportIATACode.valueOf(iata),
                AirportICAOCode.valueOf(icao),
                Coordinates.valueOf(lat, lon, elev),
                AreaCode.valueOf("PT-N"));
    }

    private static Path resourcePath(final String classpathRelative) throws URISyntaxException {
        final var url = FlightPlanJsonExporterTest.class.getClassLoader().getResource(classpathRelative);
        assertNotNull(url);
        return Path.of(url.toURI());
    }
}
