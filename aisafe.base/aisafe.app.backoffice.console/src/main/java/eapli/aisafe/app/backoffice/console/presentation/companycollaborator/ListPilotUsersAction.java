package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.framework.actions.Action;

public class ListPilotUsersAction implements Action {

    @Override
    public boolean execute() {
        return new ListPilotUsersUI().show();
    }
}
