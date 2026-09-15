package eapli.aisafe.flightmanagement.application.importfile;

import eapli.aisafe.dsl.api.ParseResult;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;

import java.util.List;
import java.util.Objects;

public final class FlightPlanFileImportResult {

    private final ParseResult parseResult;
    private final String canonicalDslContent;

    private FlightPlanFileImportResult(final ParseResult parseResult, final String canonicalDslContent) {
        this.parseResult = Objects.requireNonNull(parseResult, "parseResult");
        this.canonicalDslContent = canonicalDslContent;
    }

    public static FlightPlanFileImportResult of(final ParseResult parseResult, final String canonicalDslContent) {
        if (parseResult.isValid()) {
            return new FlightPlanFileImportResult(
                    parseResult,
                    Objects.requireNonNull(canonicalDslContent, "canonicalDslContent"));
        }
        return new FlightPlanFileImportResult(parseResult, null);
    }

    public static FlightPlanFileImportResult failure(final List<String> errors) {
        return new FlightPlanFileImportResult(ParseResult.failure(errors), null);
    }

    public static FlightPlanFileImportResult failure(final String error) {
        return failure(List.of(error));
    }

    public boolean isValid() {
        return parseResult.isValid();
    }

    public ParseResult parseResult() {
        return parseResult;
    }

    public FlightPlanDescriptor descriptor() {
        return parseResult.getDescriptor();
    }

    public List<String> errors() {
        return parseResult.getErrors();
    }

    public String canonicalDslContent() {
        return canonicalDslContent;
    }
}
