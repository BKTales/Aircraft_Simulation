package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.framework.actions.Action;

public class RegisterAircraftAction implements Action {

    @Override
    public boolean execute() {
        return new RegisterAircraftUI().show();
    }
}
