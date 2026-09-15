package eapli.aisafe.routemanagement.application;

public class RouteHasPlannedFlightsException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public RouteHasPlannedFlightsException(final String message) {
        super(message);
    }
}
