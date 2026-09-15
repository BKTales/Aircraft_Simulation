package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.framework.actions.Action;

public class ListCustomerCollaboratorUsersAction implements Action {

    @Override
    public boolean execute() {
        return new ListCustomerCollaboratorUI().show();
    }
}
