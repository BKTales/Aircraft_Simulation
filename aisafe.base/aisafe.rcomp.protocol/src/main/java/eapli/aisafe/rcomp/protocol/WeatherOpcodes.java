package eapli.aisafe.rcomp.protocol;

/** Weather Person remote commands (US044) — block 50–59. */
public final class WeatherOpcodes {

    public static final byte LIST_AREAS = 50;
    public static final byte REGISTER_WEATHER = 51;
    public static final byte BULK_IMPORT = 52;
    public static final byte CONSULT_BY_DAY = 53;

    private WeatherOpcodes() {}
}
