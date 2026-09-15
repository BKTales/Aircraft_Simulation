package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.app.backoffice.console.presentation.enginemodel.ListEngineModelsUI;
import eapli.framework.actions.Action;

public class ListAircraftModelsAction implements Action {
    @Override
    public boolean execute() {
        return new ListAircraftModelUI().show();
    }
}
