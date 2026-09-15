package eapli.aisafe.rcomp.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Encodes flight-plan file content for Pilot remote import (US081 / US121). */
public final class PilotFlightPlanPayload {

    private PilotFlightPlanPayload() {}

    public record FileContent(String fileName, byte[] bytes) {}

    public static String encodeFile(final String fileName, final byte[] content) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName is required.");
        }
        if (content == null) {
            throw new IllegalArgumentException("content is required.");
        }
        return fileName.trim() + ";" + Base64.getEncoder().encodeToString(content);
    }

    public static String encodeImport(final String fileName, final byte[] content, final String aircraftRegistration) {
        return encodeFile(fileName, content) + ";" + aircraftRegistration.trim();
    }

    public static FileContent decodeFile(final String payload) {
        final String[] parts = splitPayload(payload, 2, "fileName;base64Content");
        return new FileContent(parts[0].trim(), Base64.getDecoder().decode(parts[1].trim()));
    }

    public static FileContent decodeImport(final String payload) {
        final String[] parts = splitPayload(payload, 3, "fileName;base64Content;aircraftRegistration");
        return new FileContent(parts[0].trim(), Base64.getDecoder().decode(parts[1].trim()));
    }

    public static String aircraftRegistration(final String importPayload) {
        final String[] parts = splitPayload(importPayload, 3, "fileName;base64Content;aircraftRegistration");
        return parts[2].trim();
    }

    private static String[] splitPayload(final String payload, final int minParts, final String example) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Expected: " + example);
        }
        final String[] parts = payload.split(";", -1);
        if (parts.length < minParts) {
            throw new IllegalArgumentException("Expected: " + example);
        }
        return parts;
    }

    public static String utf8(final byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
