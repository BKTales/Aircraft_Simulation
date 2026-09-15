package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.framework.actions.Action;

public class RemovePilotCollaboratorAction implements Action {

    @Override
    public boolean execute() {
        return new RemovePilotCollaboratorUI().show();
    }
}
