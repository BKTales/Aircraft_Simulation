package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.framework.actions.Action;

public final class AddPilotExistingRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new AddPilotExistingRemoteUI().show();
    }
}
