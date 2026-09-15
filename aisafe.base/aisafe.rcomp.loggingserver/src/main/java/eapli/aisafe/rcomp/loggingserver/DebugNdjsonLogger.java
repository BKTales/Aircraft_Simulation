package eapli.aisafe.rcomp.loggingserver;

public final class DebugNdjsonLogger {

    private DebugNdjsonLogger() {
    }

    public static void log(final String runId, final String hypothesisId, final String location,
                    final String message, final String dataJson) {
        final long now = System.currentTimeMillis();
        final String payload = "{\"runId\":\"" + esc(runId)
                + "\",\"hypothesisId\":\"" + esc(hypothesisId)
                + "\",\"location\":\"" + esc(location)
                + "\",\"message\":\"" + esc(message)
                + "\",\"data\":" + (dataJson == null ? "{}" : dataJson)
                + ",\"timestamp\":" + now + "}";
        // Write to stdout so it ends up in server logs (nohup/systemd/etc).
        System.out.println("[DEBUG] " + payload);
    }

    private static String esc(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
