package eapli.aisafe.flightmanagement.application.importfile;

import java.nio.file.Path;

public final class FlightPlanFileExtensions {

    public static final String TXT = "txt";
    public static final String DSL = "dsl";

    private FlightPlanFileExtensions() {}

    public static String normalizeExtension(final Path path) {
        if (path == null) {
            return "";
        }
        final String name = path.getFileName().toString();
        final int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase();
    }

    public static boolean isSupported(final String extension) {
        return extension.isEmpty() || TXT.equals(extension) || DSL.equals(extension);
    }
}
