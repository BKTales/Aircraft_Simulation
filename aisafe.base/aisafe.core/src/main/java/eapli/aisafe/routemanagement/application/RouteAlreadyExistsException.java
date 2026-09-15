package eapli.aisafe.routemanagement.application;

public class RouteAlreadyExistsException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public RouteAlreadyExistsException(final String message) {
        super(message);
    }
}
