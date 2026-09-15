package eapli.aisafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class ImportFlightPlanFromFileAction implements Action {

    @Override
    public boolean execute() {
        return new ImportFlightPlanFromFileUI().show();
    }
}
