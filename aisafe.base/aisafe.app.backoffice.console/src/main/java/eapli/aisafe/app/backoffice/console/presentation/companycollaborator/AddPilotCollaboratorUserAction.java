package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.framework.actions.Action;

public class AddPilotCollaboratorUserAction implements Action {

    @Override
    public boolean execute() {
        return new AddPilotCollaboratorUserUI().show();
    }
}
