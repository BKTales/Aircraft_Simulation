package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;

/**
 * Same flow as backoffice {@code AddPilotCollaboratorUserUI} (new vs existing user), via TCP.
 */
public final class AddPilotCollaboratorRemoteAction implements Action {

    @Override
    public boolean execute() {
        final String[] flowOptions = {"Create User from scratch", "Use existing System User"};
        final SelectWidget<String> flowSelector = new SelectWidget<>("Operation Mode:", List.of(flowOptions));
        flowSelector.show();
        final String selectedFlow = flowSelector.selectedElement();
        if (selectedFlow == null) {
            return false;
        }
        if (selectedFlow.equals(flowOptions[0])) {
            return new AddPilotNewRemoteAction().execute();
        }
        return new AddPilotExistingRemoteAction().execute();
    }
}
