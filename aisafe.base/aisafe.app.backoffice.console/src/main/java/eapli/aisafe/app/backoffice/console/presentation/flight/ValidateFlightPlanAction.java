package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public final class ValidateFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new ValidateFlightPlanUI().show();
    }
}
