package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record RemoteActiveAircraftEntry(
        String registration,
        String modelId,
        String status,
        int totalSeats,
        int ageInYears) {

    public static List<RemoteActiveAircraftEntry> parsePayload(final String payload) {
        final List<RemoteActiveAircraftEntry> entries = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            return entries;
        }
        for (final String line : payload.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            if (parts.length < 5) {
                continue;
            }
            entries.add(new RemoteActiveAircraftEntry(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parseIntOrZero(parts[3]),
                    parseIntOrZero(parts[4])));
        }
        entries.sort(Comparator.comparing(RemoteActiveAircraftEntry::registration));
        return entries;
    }

    private static int parseIntOrZero(final String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }
}
