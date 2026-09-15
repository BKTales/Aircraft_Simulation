package eapli.aisafe.flightmanagement.application.importfile;

import eapli.aisafe.dsl.api.FlightDslParser;
import eapli.aisafe.dsl.api.ParseResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

abstract class AbstractCoreFlightDslFileImportStrategy implements FlightPlanFileImportStrategy {

    private final FlightDslParser flightDslParser;

    protected AbstractCoreFlightDslFileImportStrategy() {
        this(new FlightDslParser());
    }

    AbstractCoreFlightDslFileImportStrategy(final FlightDslParser flightDslParser) {
        this.flightDslParser = Objects.requireNonNull(flightDslParser, "flightDslParser");
    }

    @Override
    public final FlightPlanFileImportResult parse(final Path path) {
        try {
            if (!Files.exists(path)) {
                return FlightPlanFileImportResult.failure(
                        List.of("File not found: " + path.toAbsolutePath()));
            }
            if (!Files.isRegularFile(path)) {
                return FlightPlanFileImportResult.failure(
                        List.of("Not a regular file: " + path.toAbsolutePath()));
            }
            final String content = Files.readString(path, StandardCharsets.UTF_8);
            final ParseResult parsed = flightDslParser.parse(content);
            return FlightPlanFileImportResult.of(parsed, content);
        } catch (final NoSuchFileException e) {
            return FlightPlanFileImportResult.failure(
                    List.of("File not found: " + path.toAbsolutePath()));
        } catch (final IOException e) {
            return FlightPlanFileImportResult.failure(
                    List.of("Could not read file: " + path.toAbsolutePath() + " — " + e.getMessage()));
        }
    }
}
