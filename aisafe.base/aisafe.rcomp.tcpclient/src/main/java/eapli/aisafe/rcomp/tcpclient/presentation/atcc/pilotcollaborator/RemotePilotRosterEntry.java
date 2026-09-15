package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record RemotePilotRosterEntry(
        String email,
        String firstName,
        String lastName,
        String status,
        int certificationCount) {

    public static List<RemotePilotRosterEntry> parsePayload(final String payload) {
        final List<RemotePilotRosterEntry> entries = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            return entries;
        }
        for (final String line : payload.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            if (parts.length < 4) {
                continue;
            }
            final int certs = parts.length > 4 ? parseIntOrZero(parts[4]) : 0;
            entries.add(new RemotePilotRosterEntry(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    certs));
        }
        entries.sort(Comparator.comparing(RemotePilotRosterEntry::email));
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
