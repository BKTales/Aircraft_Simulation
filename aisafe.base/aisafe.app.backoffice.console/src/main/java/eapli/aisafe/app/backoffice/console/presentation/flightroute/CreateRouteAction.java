package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.framework.actions.Action;

public class CreateRouteAction implements Action {
    @Override
    public boolean execute() {
        return new CreateRouteUI().show();
    }
}
