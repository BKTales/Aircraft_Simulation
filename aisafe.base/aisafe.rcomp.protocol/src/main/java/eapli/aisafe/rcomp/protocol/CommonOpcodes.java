package eapli.aisafe.rcomp.protocol;

public final class CommonOpcodes {

    public static final byte LOGIN = 10;
    public static final byte SUCCESS_LOGIN = 11;
    public static final byte LOGOUT = 12;
    public static final byte SUCCESS_LOGOUT = 13;

    public static final byte FAILED_LOGIN = -11;
    public static final byte INVALID_CREDENTIALS = -12;

    private CommonOpcodes() {}
}
