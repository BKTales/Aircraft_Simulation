package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Aircraft model row parsed from {@code id|name|manufacturer|type} server payload. */
public record RemoteAircraftModelEntry(String id, String name, String manufacturer, String type) {

    public static List<RemoteAircraftModelEntry> parsePayload(final String payload) {
        final List<RemoteAircraftModelEntry> entries = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            return entries;
        }
        for (final String line : payload.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            entries.add(new RemoteAircraftModelEntry(
                    parts[0].trim(),
                    parts.length > 1 ? parts[1].trim() : "",
                    parts.length > 2 ? parts[2].trim() : "",
                    parts.length > 3 ? parts[3].trim() : ""));
        }
        entries.sort(Comparator.comparing(RemoteAircraftModelEntry::id));
        return entries;
    }

    @Override
    public String toString() {
        return id;
    }
}
