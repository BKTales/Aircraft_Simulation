package eapli.aisafe.rcomp.tcpclient;

public enum RemoteProfile {
    ATCC("ATCC"),
    PILOT("PILOT"),
    WEATHER("WEATHER");

    private final String loginToken;

    RemoteProfile(final String loginToken) {
        this.loginToken = loginToken;
    }

    public String loginToken() {
        return loginToken;
    }

    public static RemoteProfile fromChoice(final String choice) {
        return switch (choice == null ? "" : choice.trim()) {
            case "1" -> ATCC;
            case "2" -> PILOT;
            case "3" -> WEATHER;
            default -> null;
        };
    }
}
