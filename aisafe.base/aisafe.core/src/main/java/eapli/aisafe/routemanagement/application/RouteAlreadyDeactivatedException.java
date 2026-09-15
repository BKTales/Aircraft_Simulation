package eapli.aisafe.routemanagement.application;

public class RouteAlreadyDeactivatedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public RouteAlreadyDeactivatedException(final String message) {
        super(message);
    }
}
