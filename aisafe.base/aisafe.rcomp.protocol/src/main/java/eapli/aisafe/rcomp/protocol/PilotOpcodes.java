package eapli.aisafe.rcomp.protocol;

/** Pilot remote commands (US086) — block 40–49. */
public final class PilotOpcodes {

    public static final byte CREATE_FLIGHT_PLAN = 40;
    /** US081 / US121 — parse DSL file content from client. */
    public static final byte PARSE_FLIGHT_PLAN_FILE = 41;
    /** US081 / US121 — persist validated plan. */
    public static final byte IMPORT_FLIGHT_PLAN = 42;
    /** US081 / US121 — active aircraft registrations for import. */
    public static final byte LIST_IMPORT_AIRCRAFT = 43;
    public static final byte ATTACH_WEATHER = 44;
    public static final byte VALIDATE_FLIGHT_PLAN = 45;
    public static final byte LIST_MY_FLIGHTS = 46;
    /** US080 — active routes for manual flight-plan creation. */
    public static final byte LIST_CREATE_ROUTES = 47;
    /** US080 — active pilots in the logged-in pilot's company. */
    public static final byte LIST_COMPANY_PILOTS = 48;

    private PilotOpcodes() {}
}
