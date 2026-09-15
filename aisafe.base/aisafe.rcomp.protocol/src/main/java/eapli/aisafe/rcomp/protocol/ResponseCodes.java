package eapli.aisafe.rcomp.protocol;

public final class ResponseCodes {

    public static final byte OK = 0;
    public static final byte UNAUTHORIZED = -20;
    public static final byte FORBIDDEN = -21;
    public static final byte BAD_REQUEST = -22;
    public static final byte NOT_IMPLEMENTED = -23;
    public static final byte NEEDS_CONFIRMATION = -24;
    public static final byte NOT_FOUND = -25;
    public static final byte CONFLICT = -26;
    public static final byte INTERNAL_ERROR = -27;

    private ResponseCodes() {}
}
