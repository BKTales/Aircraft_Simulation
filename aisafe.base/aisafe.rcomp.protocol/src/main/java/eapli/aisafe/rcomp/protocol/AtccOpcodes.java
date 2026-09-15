package eapli.aisafe.rcomp.protocol;

/** ATCC remote commands (US078) — block 20–39. */
public final class AtccOpcodes {

    public static final byte LIST_AIRCRAFT_MODELS = 20;
    public static final byte REGISTER_AIRCRAFT = 21;
    public static final byte DECOMMISSION_AIRCRAFT = 22;
    public static final byte LIST_ACTIVE_AIRCRAFT = 23;
    public static final byte LIST_FLEET = 24;
    public static final byte LIST_FLEET_MODELS = 25;
    public static final byte LIST_FLEET_MANUFACTURERS = 26;

    public static final byte CREATE_ROUTE = 27;
    public static final byte DEACTIVATE_ROUTE = 28;
    public static final byte REMOVE_PILOT = 29;

    public static final byte ADD_PILOT_NEW_USER = 30;
    public static final byte ADD_PILOT_EXISTING_USER = 31;
    public static final byte LIST_PILOT_ROSTER = 32;
    public static final byte LIST_ELIGIBLE_USERS = 33;
    public static final byte LIST_AIRCRAFT_MODEL_IDS = 34;
    public static final byte LIST_CERTIFIED_ENGINES = 35;
    public static final byte ROUTE_COMPANY_CONTEXT = 36;
    public static final byte VALIDATE_ROUTE_NAME = 37;
    public static final byte LIST_ROUTE_AIRPORTS = 38;
    public static final byte LIST_ACTIVE_ROUTES = 39;

    private AtccOpcodes() {}
}
