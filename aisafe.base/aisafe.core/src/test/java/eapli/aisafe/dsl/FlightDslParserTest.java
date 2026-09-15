package eapli.aisafe.dsl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.api.FlightDslParser.FlightPlanErrorListener;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslParserTest {

    private final FlightDslParser parser = new FlightDslParser();

    private static String readResource(final String resourcePath) {
        try {
            var url = FlightDslParserTest.class.getClassLoader().getResource(resourcePath);
            assertNotNull(url, "Missing test resource: " + resourcePath);
            Path p = Path.of(url.toURI());
            return Files.readString(p);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void ensureValidFlightPlanIsAcceptedAndGettersAreCovered() {
        String input = readResource("dsl/valid.txt");

        ParseResult result = parser.parse(input);

        assertTrue(result.isValid());
        FlightPlanDescriptor descriptor = result.getDescriptor();
        LegDescriptor leg = descriptor.getLegs().get(0);

        // Chamar todos os getters para garantir coverage de 100% no LegDescriptor
        assertEquals("LPPT", leg.getDepartureAirport());
        assertEquals("EGLL", leg.getArrivalAirport());
        assertEquals("2026-04-21 14:30", leg.getDepartureTime());
        assertEquals("2026-04-21 17:15", leg.getArrivalTime());
        assertEquals(4500.50, leg.getFuelValue());
        assertEquals("kg", leg.getFuelUnit());
        assertEquals(150, descriptor.getPassengersCount());
        assertEquals(13200, descriptor.getLoadWeight());
        assertNotNull(leg.getRoute());
        assertEquals(2, leg.getRoute().segments().size());
        assertNotNull(leg.toString());
        assertNotNull(descriptor.toString());
        assertNotNull(descriptor.getFlightType());
    }

    @Test
    void ensureErrorListenerCoverageIsMaximized() {
        String input = readResource("dsl/invalid_syntax.txt");

        ParseResult result = parser.parse(input);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("Line"), "Devia conter a formatação do ErrorListener");
    }

    @Test
    void ensureLegTimeGapIsRejected() {
        String input = readResource("dsl/invalid_semantic_leg_gap_time.txt");
        ParseResult result = parser.parse(input);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("previous arrival")));
    }

    @Test
    void ensureWindDirectionOutOfRangeIsRejected() {
        String input = readResource("dsl/invalid_semantic_wind.txt");
        ParseResult result = parser.parse(input);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("wind direction")));
    }

    @Test
    void ensureSemanticErrorsReportLineAndColumn() {
        String input = readResource("dsl/invalid_semantic_wind.txt");
        ParseResult result = parser.parse(input);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.matches("Line \\d+:\\d+ - .*")),
                "Semantic errors should include line and column: " + result.getErrors());
    }

    @Test
    void ensureCoordinatesOutOfRangeIsRejected() {
        String input = readResource("dsl/invalid_semantic_coords.txt");
        ParseResult result = parser.parse(input);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("coordinates out of range")));
    }

    @Test
    void ensureSyntaxErrorIsCaptured() {
        String input = "FLIGHT ERR { TYPE REGULAR LEG { } }";

        ParseResult result = parser.parse(input);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("Line"));
    }

    @Test
    void forceNoViableAltCoverageDirectly() {
        FlightPlanErrorListener listener = new FlightPlanErrorListener();

        org.antlr.v4.runtime.NoViableAltException fakeException =
                new org.antlr.v4.runtime.NoViableAltException(null, null, null, null, null, null);

        listener.syntaxError(null, "TOKEN_TESTE", 1, 0, "Erro forçado", fakeException);

        assertFalse(listener.getErrors().isEmpty());
        assertTrue(listener.getErrors().get(0).contains("Verifique a estrutura do flight plan"));
    }
}