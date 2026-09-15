package eapli.aisafe.app.backoffice.console.presentation.enginemodel;

import eapli.framework.actions.Action;

public class ListEngineModelsAction implements Action {
    @Override
    public boolean execute() {
        return new ListEngineModelsUI().show();
    }
}

