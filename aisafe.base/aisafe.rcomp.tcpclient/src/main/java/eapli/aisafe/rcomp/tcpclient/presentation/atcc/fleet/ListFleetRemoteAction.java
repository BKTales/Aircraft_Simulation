package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.framework.actions.Action;

public final class ListFleetRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new ListFleetRemoteUI().show();
    }
}
