package eapli.aisafe.dsl.parse;

import java.util.ArrayList;
import java.util.List;

public final class SemanticErrorFormatter {

    private SemanticErrorFormatter() {}

    public static void add(final List<String> errors, final SourceSpan span, final String message) {
        if (span != null) {
            errors.add(span.prefix() + message);
        } else {
            errors.add(message);
        }
    }
}
