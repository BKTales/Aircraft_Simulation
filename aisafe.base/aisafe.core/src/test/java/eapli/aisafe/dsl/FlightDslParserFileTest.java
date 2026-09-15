package eapli.aisafe.dsl;

import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslParserFileTest {

    private final FlightDslParser parser = new FlightDslParser();

    private static Path resourcePath(final String classpathRelative) throws URISyntaxException {
        var url = FlightDslParserFileTest.class.getClassLoader().getResource(classpathRelative);
        assertNotNull(url);
        return Path.of(url.toURI());
    }

    @Test
    void parsesValidTxtFromClasspathResource(@TempDir final Path tmp) throws IOException, URISyntaxException {
        Path src = resourcePath("dsl/valid.txt");
        Path copy = tmp.resolve("plan.txt");
        Files.copy(src, copy);

        ParseResult result = parser.parse(copy);

        assertTrue(result.isValid());
        assertNotNull(result.getDescriptor());
        assertEquals("valid", result.getDescriptor().getFlightId());
    }

    @Test
    void rejectsMissingFile() {
        ParseResult result = parser.parse(Path.of("/no/such/flight_plan_file_081.txt"));
        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).toLowerCase().contains("not found"));
    }

    @Test
    void parsesValidDslWithNonTxtExtension(@TempDir final Path tmp) throws IOException, URISyntaxException {
        Path src = resourcePath("dsl/valid.txt");
        Path copy = tmp.resolve("plan.dsl");
        Files.copy(src, copy);

        ParseResult result = parser.parse(copy);

        assertTrue(result.isValid());
        assertEquals("valid", result.getDescriptor().getFlightId());
    }
}
