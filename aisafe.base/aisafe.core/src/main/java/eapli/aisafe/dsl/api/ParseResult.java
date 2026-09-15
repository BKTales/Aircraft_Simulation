package eapli.aisafe.dsl.api;

import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import java.util.List;

public class ParseResult {

    private final FlightPlanDescriptor descriptor;
    private final List<String> errors;
    private final boolean valid;

    private ParseResult(FlightPlanDescriptor descriptor, List<String> errors, boolean valid) {
        this.descriptor = descriptor;
        this.errors = errors;
        this.valid = valid;
    }

    public static ParseResult success(FlightPlanDescriptor d) {
        return new ParseResult(d, List.of(), true);
    }

    public static ParseResult failure(List<String> errors) {
        return new ParseResult(null, errors, false);
    }

    public boolean isValid()                    { return valid; }
    public FlightPlanDescriptor getDescriptor() { return descriptor; }
    public List<String> getErrors()             { return errors; }
}