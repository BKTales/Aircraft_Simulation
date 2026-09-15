package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import java.util.ArrayList;
import java.util.List;

public record RemotePilotEntry(String username, String firstName, String lastName, String email) {

    public static List<RemotePilotEntry> parseLines(final String payload) {
        final List<RemotePilotEntry> entries = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            return entries;
        }
        for (final String line : payload.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            if (parts.length >= 4) {
                entries.add(new RemotePilotEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim()));
            }
        }
        return entries;
    }
}
