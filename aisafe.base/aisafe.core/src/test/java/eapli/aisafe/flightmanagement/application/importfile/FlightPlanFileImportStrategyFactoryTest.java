package eapli.aisafe.flightmanagement.application.importfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanFileImportStrategyFactoryTest {

    private final FlightPlanFileImportStrategyFactory factory = new FlightPlanFileImportStrategyFactory();

    @Test
    void rejectsJsonExtension(@TempDir Path dir) throws Exception {
        final Path json = dir.resolve("plan.json");
        Files.writeString(json, "{}");

        final FlightPlanFileImportResult result = factory.parse(json);

        assertFalse(result.isValid());
        assertTrue(result.errors().get(0).contains("Unsupported file format"));
        assertTrue(result.errors().get(0).contains(".json"));
    }

    @Test
    void supportsTxtDslAndNoExtension(@TempDir Path dir) throws Exception {
        assertTrue(factory.isSupportedPath(dir.resolve("a.txt")));
        assertTrue(factory.isSupportedPath(dir.resolve("b.dsl")));
        assertTrue(factory.isSupportedPath(dir.resolve("flightplan")));
        assertFalse(factory.isSupportedPath(dir.resolve("c.json")));
    }

    @Test
    void normalizeExtension() {
        assertEquals("txt", FlightPlanFileExtensions.normalizeExtension(Path.of("x.TXT")));
        assertEquals("dsl", FlightPlanFileExtensions.normalizeExtension(Path.of("plan.Dsl")));
        assertEquals("", FlightPlanFileExtensions.normalizeExtension(Path.of("noext")));
    }
}
