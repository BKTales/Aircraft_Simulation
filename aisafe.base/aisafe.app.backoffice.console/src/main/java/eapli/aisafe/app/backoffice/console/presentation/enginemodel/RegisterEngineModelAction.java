package eapli.aisafe.app.backoffice.console.presentation.enginemodel;

import eapli.framework.actions.Action;

public class RegisterEngineModelAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterEngineModelUI().show();
    }
}

