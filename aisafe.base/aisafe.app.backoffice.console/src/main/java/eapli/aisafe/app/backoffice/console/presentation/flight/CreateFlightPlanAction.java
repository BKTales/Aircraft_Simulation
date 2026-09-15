package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class CreateFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new CreateFlightPlanUI().show();
    }
}
