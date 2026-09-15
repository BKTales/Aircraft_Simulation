package eapli.aisafe;

public final class Application {
    public static final String VERSION = "1.0.0";
    public static final String COPYRIGHT = "(C) 2026 AISafe";

    private static final AppSettings SETTINGS = new AppSettings();

    private Application() {
    }

    public static AppSettings settings() {
        return SETTINGS;
    }
}
