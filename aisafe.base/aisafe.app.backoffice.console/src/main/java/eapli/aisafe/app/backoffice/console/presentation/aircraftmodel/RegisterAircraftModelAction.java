package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.framework.actions.Action;

public class RegisterAircraftModelAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterAircraftModelUI().show();
    }
}
