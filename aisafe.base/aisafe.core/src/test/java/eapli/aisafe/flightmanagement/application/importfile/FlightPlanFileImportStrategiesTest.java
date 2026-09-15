package eapli.aisafe.flightmanagement.application.importfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanFileImportStrategiesTest {

    @ParameterizedTest
    @MethodSource("extensionSupportCases")
    void supportsOnlyItsExtension(
            final FlightPlanFileImportStrategy strategy,
            final String supported,
            final String unsupported) {
        assertTrue(strategy.supports(supported));
        assertFalse(strategy.supports(unsupported));
        assertFalse(strategy.supports("json"));
    }

    static Stream<Arguments> extensionSupportCases() {
        return Stream.of(
                Arguments.of(new TxtFlightPlanFileImportStrategy(), "txt", "dsl"),
                Arguments.of(new DslFlightPlanFileImportStrategy(), "dsl", "txt"),
                Arguments.of(new ExtensionlessFlightPlanFileImportStrategy(), "", "txt"));
    }

    @Test
    void txtStrategyParsesValidFixture() throws Exception {
        final var strategy = new TxtFlightPlanFileImportStrategy();
        final Path path = classpathResource("dsl/valid.txt");
        final FlightPlanFileImportResult result = strategy.parse(path);

        assertTrue(result.isValid(), () -> String.join("; ", result.errors()));
        assertNotNull(result.descriptor());
        assertFalse(result.canonicalDslContent().isBlank());
    }

    @Test
    void dslStrategyParsesRenamedFixture(@TempDir Path dir) throws Exception {
        final Path source = classpathResource("dsl/valid.txt");
        final Path dsl = dir.resolve("plan.dsl");
        Files.copy(source, dsl);

        final FlightPlanFileImportResult result = new DslFlightPlanFileImportStrategy().parse(dsl);

        assertTrue(result.isValid());
        assertEquals(Files.readString(source), result.canonicalDslContent());
    }

    @Test
    void extensionlessStrategyParsesCopy(@TempDir Path dir) throws Exception {
        final Path source = classpathResource("dsl/valid.txt");
        final Path noExt = dir.resolve("flightplan");
        Files.copy(source, noExt);

        final FlightPlanFileImportResult result =
                new ExtensionlessFlightPlanFileImportStrategy().parse(noExt);

        assertTrue(result.isValid());
    }

    private static Path classpathResource(final String resourcePath) throws Exception {
        final var url = FlightPlanFileImportStrategiesTest.class.getClassLoader().getResource(resourcePath);
        assertNotNull(url, "missing test resource: " + resourcePath);
        return Path.of(url.toURI());
    }
}
