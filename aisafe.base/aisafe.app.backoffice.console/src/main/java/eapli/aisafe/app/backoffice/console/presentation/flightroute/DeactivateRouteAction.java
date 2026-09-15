package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.framework.actions.Action;

public class DeactivateRouteAction implements Action {
    @Override
    public boolean execute() {
        return new DeactivateRouteUI().show();
    }
}
