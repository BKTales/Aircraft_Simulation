package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Airport row parsed from {@code iata|icao|area} server payload. */
public record RemoteAirportEntry(String iata, String icao, String area) {

    public static List<RemoteAirportEntry> parsePayload(final String payload) {
        final List<RemoteAirportEntry> entries = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            return entries;
        }
        for (final String line : payload.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            entries.add(new RemoteAirportEntry(
                    parts[0].trim(),
                    parts.length > 1 ? parts[1].trim() : "",
                    parts.length > 2 ? parts[2].trim() : ""));
        }
        entries.sort(Comparator.comparing(RemoteAirportEntry::iata));
        return entries;
    }
}
