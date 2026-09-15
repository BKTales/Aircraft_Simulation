package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.framework.actions.Action;

public class AddCustomerCollaboratorUserAction implements Action {

    @Override
    public boolean execute() {
        return new AddCustomerCollaboratorUserUI().show();
    }
}
