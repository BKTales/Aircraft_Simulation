package eapli.aisafe.flightmanagement.application;

import java.util.List;
import java.util.Locale;

final class SimulatorFailureMessages {

    private SimulatorFailureMessages() {}

    static String summarize(final List<String> outputLines, final int exitCode) {
        if (outputLines != null) {
            for (final String line : outputLines) {
                if (line == null) {
                    continue;
                }
                final String lower = line.toLowerCase(Locale.ROOT);
                if (lower.contains("all flights are invalid")) {
                    return "Flight simulator rejected the plan: fuel, payload, or take-off weight exceeds aircraft limits.";
                }
                if (lower.contains("invalid:") && lower.contains("fuel")) {
                    return "Flight simulator rejected the plan: leg fuel exceeds aircraft tank capacity.";
                }
                if (lower.contains("invalid:") && lower.contains("payload")) {
                    return "Flight simulator rejected the plan: payload exceeds aircraft maximum payload.";
                }
                if (lower.contains("invalid:") && lower.contains("mtow")) {
                    return "Flight simulator rejected the plan: take-off weight exceeds MTOW.";
                }
            }
        }
        return "Flight simulator exited with code " + exitCode;
    }
}
