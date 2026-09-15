package eapli.aisafe.rcomp.server.atcc;

import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AtccPayloadParser {

    private AtccPayloadParser() {}

    public static String[] splitFields(final String payload) {
        if (payload == null || payload.isBlank()) {
            return new String[0];
        }
        return payload.split(";", -1);
    }

    public static String registrationOnly(final String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Registration is required.");
        }
        return payload.trim();
    }

    public static List<PilotCertificationSpec> parseCertifications(final String payload) {
        final List<PilotCertificationSpec> specs = new ArrayList<>();
        if (payload == null) {
            return specs;
        }
        for (final String token : payload.split(";")) {
            final String trimmed = token.trim();
            if (trimmed.startsWith("CERT:")) {
                final String[] certParts = trimmed.substring(5).split(",", -1);
                if (certParts.length != 3) {
                    throw new IllegalArgumentException("CERT format: CERT:modelId,from,to");
                }
                specs.add(new PilotCertificationSpec(
                        certParts[0].trim(), certParts[1].trim(), certParts[2].trim()));
            }
        }
        return specs;
    }

    /** Payload without CERT tokens — base fields only. */
    public static String stripCertTokens(final String payload) {
        if (payload == null) {
            return "";
        }
        return Arrays.stream(payload.split(";"))
                .map(String::trim)
                .filter(s -> !s.startsWith("CERT:"))
                .reduce((a, b) -> a + ";" + b)
                .orElse("");
    }
}
