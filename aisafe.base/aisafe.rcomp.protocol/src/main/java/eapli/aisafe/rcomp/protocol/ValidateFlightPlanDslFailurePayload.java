package eapli.aisafe.rcomp.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * BAD_REQUEST body for a US085 DSL parse failure. Carries the original DSL
 * source plus the parse errors (Base64-encoded) so the client can render the
 * same caret report used by US121.
 *
 * Wire format: {@code DSL_FAILURE|<base64 dsl>|<base64 errors joined by \n>}
 */
public final class ValidateFlightPlanDslFailurePayload {

    public static final String MARKER = "DSL_FAILURE";

    private ValidateFlightPlanDslFailurePayload() {}

    public static boolean matches(final String payload) {
        return payload != null && payload.startsWith(MARKER + "|");
    }

    public static String encode(final String dslContent, final List<String> errors) {
        final String dsl = dslContent == null ? "" : dslContent;
        final String joined = errors == null ? "" : String.join("\n", errors);
        return MARKER + "|" + base64(dsl) + "|" + base64(joined);
    }

    public static Decoded decode(final String payload) {
        final String[] parts = payload.split("\\|", 3);
        final String dsl = parts.length > 1 ? fromBase64(parts[1]) : "";
        final String joined = parts.length > 2 ? fromBase64(parts[2]) : "";
        final List<String> errors = joined.isEmpty() ? List.of() : List.of(joined.split("\n", -1));
        return new Decoded(dsl, errors);
    }

    private static String base64(final String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String fromBase64(final String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public record Decoded(String dslContent, List<String> errors) {}
}
